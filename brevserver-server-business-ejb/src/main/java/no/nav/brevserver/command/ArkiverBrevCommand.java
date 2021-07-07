package no.nav.brevserver.command;

import no.nav.brevserver.converter.BrevstatusTilVoConverter;
import no.nav.brevserver.converter.VoTilBrevstatusConverter;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.ArgumentValidator;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.brevlager.BrevlagerService;
import no.nav.brevserver.service.brevlager.BrevlagerServiceFactory;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import no.nav.brevserver.service.jms.MessageProducer;
import no.nav.brevserver.service.jms.MessageProducerFactory;

/**
 * H�ndterer meldinger fra Dialogue. Lagrer brev og setter status og sender kvittering til saksbehandlingsystemet.
 *
 * @author Holger Zobel, Accenture
 */
public class ArkiverBrevCommand extends AbstractCommand {
	BrevStatusVO brevStatusVo;
	private KvitteringVO kvittering;
	private VoTilBrevstatusConverter converter = new VoTilBrevstatusConverter();
	private BrevstatusTilVoConverter converterToVo = new BrevstatusTilVoConverter();

	public ArkiverBrevCommand(MessageVO msg) {
		super(msg);
	}

	private void validate() throws BrevTechnicalException {
		String methSig = "ArkiverBrevCommand.validate()";

		try {
			kvittering = DialogueXMLParser.lagKvitteringVOFraDialogueMelding(messageVo.getByteBody());
		} catch (BrevTechnicalException e) {
			log.error(methSig, "Ugyldig XML: " + messageVo.getStringBody(), e);
			throw e;
		}

		if (kvittering.getSystemID().startsWith(SystemType.PE.toString())) {
			String errorMessage = "Brev med feil systemID mottatt: '" + kvittering.getSystemID()
					+ "', forventet ikke pensjonsbrev";
			log.error(methSig, errorMessage);
			throw new BrevTechnicalException(errorMessage);
		}

		messageVo.setBrevreferanse(kvittering.getBrevreferanse());
	}

	/**
	 * Foresp�rsel lagres i databasen. Deretter sendes den originale meldingen videre p� definert k�.
	 *
	 * @see AbstractCommand#execute()
	 */
	public void execute() throws BrevException {
		validate();

		ArgumentValidator.isNotNull(kvittering);

		messageVo.setBrevreferanse(kvittering.getBrevreferanse());
		String sig = "ArkiverBrevCommand.execute(" + kvittering.getBrevreferanse() + ")";

		PerformanceLogger p = new PerformanceLogger(sig);

		try {
			BrevserverService brevserverService = BrevserverServiceFactory.getInstance().createBrevserverService();
			Brevstatus brevstatus = brevserverService.hentBrevStatus(kvittering.getSystemID(), kvittering.getBrevreferanse());

			// Hvis ingen status s� opprett en basert p� det man vet
			if (brevstatus == null) {
				brevstatus = new Brevstatus();
			}
			if (brevstatus.getSystemID() == null) {
				brevstatus.setSystemID(kvittering.getSystemID());
			}
			if (brevstatus.getBrevreferanse() == null) {
				brevstatus.setBrevreferanse(kvittering.getBrevreferanse());
			}
			if (brevstatus.getBrevmal() == null) {
				brevstatus.setBrevmal(kvittering.getTmpMalpakke());
			}
			if (brevstatus.getReturKoe() == null) {
				brevstatus.setReturKoe(messageVo.getReplyQueueName());
			}

			// Hvis feilniv� er 0x s� endre til x
			if (kvittering.getFeilniva() != null && kvittering.getFeilniva().length() > 1
					&& kvittering.getFeilniva().charAt(0) == '0') {
				kvittering.setFeilniva(kvittering.getFeilniva().substring(1));
			}

			// Hvis brevet eksisterer allerede s� gi feilmelding
			if (Konstanter.BREVSTATUS_FERDIG.equals(brevstatus.getStatus())) {
				kvittering.setFeilkode(Konstanter.FEIL_BREV_EKSISTERER);
				brevstatus.setStatus(Konstanter.BREVSTATUS_FEIL);

				// Ved feilmelding fra dialogue s� gi feilmelding
			} else if (kvittering.getFeilniva() == null ||
					kvittering.getFeilniva().equals(Konstanter.BREVPAKKE_FEILNIVA_FEIL)) {
				brevstatus.setStatus(Konstanter.BREVSTATUS_FEIL);

				if (brevstatus.getBrevreferanse() != null && brevstatus.getSystemID() != null) {
					brevserverService.lagreBrevStatus(brevstatus, brevStatusVo.getToken());
				}

				// Alt gikk bra
			} else {
				if (FilType.PDF.getContentType().equals(kvittering.getContentType())) {
					kvittering.setLagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG);
					brevstatus.setStatus(Konstanter.BREVSTATUS_FERDIG);
				} else {
					kvittering.setLagerStatus(Konstanter.BREVLAGER_STATUS_KLADD);
					brevstatus.setStatus(Konstanter.BREVSTATUS_LAGRET_KLADD);
				}
				// Lagre i Brevlageret
				BrevlagerService brevlagerService = BrevlagerServiceFactory.getInstance().createBrevlagerService();
				brevlagerService.lagreBrev(kvittering, brevstatus, brevStatusVo.getToken());

				log.info(sig, "Brevet er arkivert i Brevlageret");
			}

			MessageProducer producer = MessageProducerFactory.getInstance().createMessageProducer(SystemType.BI);
			//TODO: Update brevstatusVO Object
			producer.sendKvittering(brevStatusVo, messageVo, kvittering);

		} finally {
			p.stop();
		}
	}

	/**
	 * Returnerer status-objekt. Dette innholder bla. brevref og returk� og kan benyttes for � sende feilmeldinger.
	 */
	public Object getResult() {
		return brevStatusVo;
	}
}
