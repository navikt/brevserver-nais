package no.nav.brevserver.arkiverBrev.Pesys;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.utils.ExchangeUtils;
import no.nav.brevserver.core.utils.xmlHandlers.DialogueXMLParser;
import no.nav.brevserver.core.utils.xmlHandlers.XMLService;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.core.vo.KvitteringVO;
import no.nav.brevserver.core.vo.MessageVO;
import no.nav.brevserver.joark.JoarkService;
import no.nav.brevserver.service.BrevstatusService;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_TILBAKEMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.setBodyAndReturnQueueWithMode;

@Slf4j
@Service
public class PeArkiverBrevService {

	private final BrevstatusService brevstatusService;
	private final JoarkService dokarkivService;

	public PeArkiverBrevService(
			BrevstatusService brevstatusService,
			@Qualifier("dokarkivService") JoarkService dokarkivService) {
		this.brevstatusService = brevstatusService;
		this.dokarkivService = dokarkivService;
	}

	@SuppressWarnings("unused")
	@Handler
	public void execute(Exchange exchange) throws BrevException {
		//Konverter Exchange til messageVo
		MessageVO messageVo = ExchangeUtils.getMessageVoFromExchange(exchange);
		//Marshall xml'en til businessobjekt
		KvitteringVO kvittering = generateKvittering(messageVo);
		log.info("Mottat kvittering for brevreferanse: " + kvittering.getBrevreferanse());

		// Sjekk om brevet finnes, hent status
		BrevStatusVO brevStatusVo = brevstatusService.hentBrevStatus(kvittering.getBrevreferanse(), kvittering.getSystemID());

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
			dokarkivService.lagreDokument(kvittering.getBrevreferanse(), kvittering.getContentType(), kvittering.getBrevdata());

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
			brevstatusService.lagreBrevStatus(brevStatusVo);
			log.info("Brev med brevref: " + brevStatusVo.getBrevreferanse() + " er arkivert i Brevlageret");
		}

		setBodyAndReturnQueueWithMode(exchange,
				createReturKvittering(brevStatusVo, kvittering),
				brevStatusVo.getReturKoe(),
				GI_TILBAKEMELDING);
		exchange.getIn().setHeader("JMS_IBM_Format", "MQSTR");
	}


	private KvitteringVO generateKvittering(MessageVO messageVo) throws BrevFunctionalException {
		KvitteringVO kvitteringVo;

		try {
			kvitteringVo = DialogueXMLParser.lagKvitteringVOFraDialogueMelding(messageVo.getByteBody());
		} catch (Exception e) {
			throw new BrevFunctionalException("Ugyldig brev-xml: \n" + e.getMessage());
		}

		if (!kvitteringVo.getSystemID().startsWith(SystemType.PE.toString())) {
			String errorMessage = "Brev med feil systemID mottatt: '" + kvitteringVo.getSystemID()
					+ "', forventet bidragsbrev!";
			log.error(errorMessage);
			throw new BrevFunctionalException(errorMessage);
		}

		return kvitteringVo;
	}


	private String createReturKvittering(BrevStatusVO brevStatusVo, KvitteringVO kvittering) throws BrevFunctionalException {
		if (brevStatusVo == null) {
			throw new BrevFunctionalException("Kunne ikke lage kvittering da brevstatus er null");
		}

		return XMLService.unmarshal(kvittering, brevStatusVo);
	}
}
