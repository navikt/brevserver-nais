package no.nav.brevserver.command;

import no.nav.brevserver.consumer.joark.JoarkServiceBi;
import no.nav.brevserver.consumer.joark.factory.JoarkServiceBeanFactory;
import no.nav.brevserver.converter.BrevstatusTilVoConverter;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import no.nav.brevserver.service.jms.MessageProducer;
import no.nav.brevserver.service.jms.MessageProducerFactory;

/**
 * Beskrivelse av klassen
 *
 * @author Dag Kristiansen
 */
public class PEArkiverBrevCommand extends AbstractCommand {
	private Brevstatus brevstatus;
	private KvitteringVO kvittering;
	private BrevstatusTilVoConverter converter = new BrevstatusTilVoConverter();

	public PEArkiverBrevCommand(MessageVO messageVO) {
		super(messageVO);
	}

	private void validate() throws BrevTechnicalException {
		String methSig = "PEBestillBrevCommand.validate()";

		brevstatus = Brevstatus.builder()
				.returKoe(messageVo.getReplyQueueName())
				.build();
		kvittering = DialogueXMLParser.lagKvitteringVOFraDialogueMelding(messageVo.getByteBody());

		if (!kvittering.getSystemID().startsWith(SystemType.PE.toString())) {
			String errorMessage = "Brev med feil systemID mottatt: '" + kvittering.getSystemID() + "', forventet prefix: '"
					+ SystemType.PE + "'";
			log.error(methSig, errorMessage);
			throw new BrevTechnicalException(errorMessage);
		}
	}

	public void execute() throws BrevException {
		validate();

		BrevserverService brevserverService = BrevserverServiceFactory.getInstance().createBrevserverService();
		// Sjekk om brevet finnes, hent status
		brevstatus = brevserverService.hentBrevStatus(kvittering.getSystemID(), kvittering.getBrevreferanse());

		// Hvis ingen status så opprett en basert på det man vet
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

		// Hvis feilnivå er 0x så endre til x
		if (kvittering.getFeilniva() != null && kvittering.getFeilniva().length() > 1
				&& kvittering.getFeilniva().charAt(0) == '0') {
			kvittering.setFeilniva(kvittering.getFeilniva().substring(1));
		}

		// Hvis brevet eksisterer allerede så gi feilmelding
		if (Konstanter.BREVSTATUS_FERDIG.equals(brevstatus.getStatus())) {
			kvittering.setFeilkode(Konstanter.FEIL_BREV_EKSISTERER);
			brevstatus.setStatus(Konstanter.BREVSTATUS_FEIL);

			// Ved feilmelding fra dialogue så gi feilmelding
		} else if (kvittering.getFeilniva() == null || kvittering.getFeilniva().equals(Konstanter.BREVPAKKE_FEILNIVA_FEIL)) {
			brevstatus.setStatus(Konstanter.BREVSTATUS_FEIL);

			// Alt gikk bra, lagre i JOARK.
		} else {
			JoarkServiceBi joarkService = JoarkServiceBeanFactory.getInstance().getJoarkService();
			joarkService.lagreDokument(kvittering.getBrevreferanse(), kvittering.getContentType(), kvittering.getBrevdata());

			if (FilType.PDF.getContentType().equals(kvittering.getContentType())) {
				kvittering.setLagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG);
				brevstatus.setStatus(Konstanter.BREVSTATUS_FERDIG);
			} else if (FilType.RTF.getContentType().equals(kvittering.getContentType())
					|| FilType.DOCX.getContentType().equals(kvittering.getContentType())) {
				kvittering.setLagerStatus(Konstanter.BREVLAGER_STATUS_KLADD);
				brevstatus.setStatus(Konstanter.BREVSTATUS_LAGRET_KLADD);
			} else {
				throw new RuntimeException("Unknown file format!");
			}
		}

		if (brevstatus.getBrevreferanse() != null && brevstatus.getSystemID() != null) {
			brevserverService.lagreBrevStatus(brevstatus, null);
		}

		MessageProducer producer = MessageProducerFactory.getInstance().createMessageProducer(SystemType.PE);
		BrevStatusVO brevStatusVO = converter.convert(brevstatus);
		producer.sendKvittering(brevStatusVO, messageVo, kvittering);
	}

	public Object getResult() {
		return brevstatus;
	}
}
