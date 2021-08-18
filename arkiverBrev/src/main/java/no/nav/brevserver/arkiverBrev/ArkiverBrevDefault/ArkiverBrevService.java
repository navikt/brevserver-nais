package no.nav.brevserver.arkiverBrev.ArkiverBrevDefault;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.arkiverBrev.DialogueXMLParser;
import no.nav.brevserver.arkiverBrev.XMLService;
import no.nav.brevserver.arkiverBrev.util.Utils;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;

/**
 * Håndterer meldinger fra Dialogue. Lagrer brev og setter status og sender kvittering til saksbehandlingsystemet.
 *
 * @author Holger Zobel, Accenture
 */
@Slf4j
@Component
public class ArkiverBrevService {

	private BrevstatusService brevstatusService;
	private BrevlagerService brevlagerService;
	private XMLService xmlService;

	public ArkiverBrevService(
							  BrevstatusService brevstatusService,
							  BrevlagerService brevlagerService,
							  XMLService xmlService) {
		this.brevstatusService = brevstatusService;
		this.brevlagerService = brevlagerService;
		this.xmlService = xmlService;
	}

	@Handler
	public void execute(Exchange exchange) throws BrevException, JMSException {

		MessageVO messageVo = Utils.createMessageVoFromExchange(exchange);

		KvitteringVO kvittering = generateKvittering(messageVo);

		if (kvittering == null) {
			throw new BrevFunctionalException("Kvittering er null");
		}

		messageVo.setBrevreferanse(kvittering.getBrevreferanse());

		BrevStatusVO brevStatusVo = brevstatusService.hentBrevStatus(kvittering.getSystemID(), kvittering.getBrevreferanse());
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
				brevstatusService.lagreBrevStatus(brevStatusVo);
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
			brevlagerService.lagreBrev(kvittering, brevStatusVo);

			log.info("Brevet er arkivert i Brevlageret");
		}

		String message = createKvitteringsXml(brevStatusVo, kvittering);
		exchange.getIn().setBody(message);

	}


	private KvitteringVO generateKvittering(MessageVO messageVo) throws BrevTechnicalException {
		KvitteringVO kvitteringVo;

		try {
			kvitteringVo = DialogueXMLParser.lagKvitteringVOFraDialogueMelding(messageVo.getByteBody());
		} catch (BrevTechnicalException e) {
			log.error("Ugyldig XML: ", e);
			throw e;
		}

		if (kvitteringVo.getSystemID().startsWith(SystemType.PE.toString())) {
			String errorMessage = "Brev med feil systemID mottatt: '" + kvitteringVo.getSystemID()
					+ "', forventet ikke pensjonsbrev";
			log.error(errorMessage);
			throw new BrevTechnicalException(errorMessage);
		}

		return kvitteringVo;
	}

	private String createKvitteringsXml(BrevStatusVO brevStatusVo, KvitteringVO kvittering) throws BrevFunctionalException {
		if (brevStatusVo == null) {
			throw new BrevFunctionalException("Kunne ikke lage kvittering da enten brevstatus er null");
		}

		//Denne trengs kanskje ikke.
		//Alt som kommer gjennom her skal vel egentlig til samme kø hver gang
		if (brevStatusVo.getReturKoe() == null || "".equals(brevStatusVo.getReturKoe())) {
			throw new BrevFunctionalException("Kan ikke sende kvittering da returkø mangler");
		}

		return xmlService.unmarshal(kvittering, brevStatusVo);

	}

}

