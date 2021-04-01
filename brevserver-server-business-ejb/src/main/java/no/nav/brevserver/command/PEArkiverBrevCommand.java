package no.nav.brevserver.command;

import no.nav.brevserver.consumer.joark.JoarkServiceBi;
import no.nav.brevserver.consumer.joark.factory.JoarkServiceBeanFactory;
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
	private BrevStatusVO brevStatusVo;
	private KvitteringVO kvittering;

	public PEArkiverBrevCommand(MessageVO messageVO) {
		super(messageVO);
	}

	private void validate() throws BrevTechnicalException {
		String methSig = "PEBestillBrevCommand.validate()";

		brevStatusVo = new BrevStatusVO();
		brevStatusVo.setReturKoe(messageVo.getReplyQueueName());
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
		} else if (kvittering.getFeilniva() == null || kvittering.getFeilniva().equals(Konstanter.BREVPAKKE_FEILNIVA_FEIL)) {
			brevStatusVo.setStatus(Konstanter.BREVSTATUS_FEIL);

			// Alt gikk bra, lagre i JOARK.
		} else {
			JoarkServiceBi joarkService = JoarkServiceBeanFactory.getInstance().getJoarkService();
			joarkService.lagreDokument(kvittering.getBrevreferanse(), kvittering.getContentType(), kvittering.getBrevdata());

			if (FilType.PDF.getContentType().equals(kvittering.getContentType())) {
				kvittering.setLagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG);
				brevStatusVo.setStatus(Konstanter.BREVSTATUS_FERDIG);
			} else if (FilType.RTF.getContentType().equals(kvittering.getContentType())
					|| FilType.DOCX.getContentType().equals(kvittering.getContentType())) {
				kvittering.setLagerStatus(Konstanter.BREVLAGER_STATUS_KLADD);
				brevStatusVo.setStatus(Konstanter.BREVSTATUS_LAGRET_KLADD);
			} else {
				throw new RuntimeException("Unknown file format!");
			}
		}

		if (brevStatusVo.getBrevreferanse() != null && brevStatusVo.getSystemID() != null) {
			brevserverService.lagreBrevStatus(brevStatusVo);
		}

		MessageProducer producer = MessageProducerFactory.getInstance().createMessageProducer(SystemType.PE);
		producer.sendKvittering(brevStatusVo, messageVo, kvittering);
	}

	public Object getResult() {
		return brevStatusVo;
	}
}
