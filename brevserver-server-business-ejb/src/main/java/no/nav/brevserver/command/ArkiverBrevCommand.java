package no.nav.brevserver.command;

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
 * Håndterer meldinger fra Dialogue. Lagrer brev og setter status og sender kvittering til saksbehandlingsystemet.
 *
 * @author Holger Zobel, Accenture
 */
public class ArkiverBrevCommand extends AbstractCommand {
	BrevStatusVO brevStatusVo;
	private KvitteringVO kvittering;

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
	 * Forespørsel lagres i databasen. Deretter sendes den originale meldingen videre på definert kø.
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
			brevStatusVo = brevserverService.hentBrevStatus(kvittering.getSystemID(), kvittering.getBrevreferanse());

			// Hvis ingen status så opprett en basert på det man vet
			if (brevStatusVo == null) {
				brevStatusVo = new BrevStatusVO();
			}
			if (brevStatusVo.getSystemID() == null) {
				brevStatusVo.setSystemID(kvittering.getSystemID());
			}
			if (brevStatusVo.getBrevreferanse() == null) {
				brevStatusVo.setBrevreferanse(kvittering.getBrevreferanse());
			}
			if (brevStatusVo.getBrevmal() == null) {
				brevStatusVo.setBrevmal(kvittering.getTmpMalpakke());
			}
			if (brevStatusVo.getReturKoe() == null) {
				brevStatusVo.setReturKoe(messageVo.getReplyQueueName());
			}

			// Hvis feilnivå er 0x så endre til x
			if (kvittering.getFeilniva() != null && kvittering.getFeilniva().length() > 1
					&& kvittering.getFeilniva().charAt(0) == '0') {
				kvittering.setFeilniva(kvittering.getFeilniva().substring(1));
			}

			// Hvis brevet eksisterer allerede så gi feilmelding
			if (Konstanter.BREVSTATUS_FERDIG.equals(brevStatusVo.getStatus())) {
				kvittering.setFeilkode(Konstanter.FEIL_BREV_EKSISTERER);
				brevStatusVo.setStatus(Konstanter.BREVSTATUS_FEIL);

				// Ved feilmelding fra dialogue så gi feilmelding
			} else if (kvittering.getFeilniva() == null ||
					kvittering.getFeilniva().equals(Konstanter.BREVPAKKE_FEILNIVA_FEIL)) {
				brevStatusVo.setStatus(Konstanter.BREVSTATUS_FEIL);

				if (brevStatusVo.getBrevreferanse() != null && brevStatusVo.getSystemID() != null) {
					brevserverService.lagreBrevStatus(brevStatusVo);
				}

				// Alt gikk bra
			} else {
				if (FilType.PDF.getContentType().equals(kvittering.getContentType())) {
					kvittering.setLagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG);
					brevStatusVo.setStatus(Konstanter.BREVSTATUS_FERDIG);
				} else {
					kvittering.setLagerStatus(Konstanter.BREVLAGER_STATUS_KLADD);
					brevStatusVo.setStatus(Konstanter.BREVSTATUS_LAGRET_KLADD);
				}
				// Lagre i Brevlageret
				BrevlagerService brevlagerService = BrevlagerServiceFactory.getInstance().createBrevlagerService();
				brevlagerService.lagreBrev(kvittering, brevStatusVo);

				log.info(sig, "Brevet er arkivert i Brevlageret");
			}

			MessageProducer producer = MessageProducerFactory.getInstance().createMessageProducer(SystemType.BI);
			producer.sendKvittering(brevStatusVo, messageVo, kvittering);

		} finally {
			p.stop();
		}
	}

	/**
	 * Returnerer status-objekt. Dette innholder bla. brevref og returkø og kan benyttes for å sende feilmeldinger.
	 */
	public Object getResult() {
		return brevStatusVo;
	}
}
