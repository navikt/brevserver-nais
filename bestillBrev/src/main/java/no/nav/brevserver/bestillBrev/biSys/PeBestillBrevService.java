package no.nav.brevserver.bestillBrev.biSys;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.utils.mqUtils.Utils;
import no.nav.brevserver.core.utils.xmlHandlers.XMLService;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.KvitteringVO;
import no.nav.brevserver.core.vo.MessageVO;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.io.StringReader;

import static no.nav.brevserver.core.utils.mqUtils.Utils.notEmpty;

@Slf4j
@Component
public class PeBestillBrevService {

	private final BrevstatusService brevstatusService;
	private final BrevtilgangService brevtilgangService;


	public PeBestillBrevService(BrevstatusService brevstatusService,
								BrevtilgangService brevtilgangService) {
		this.brevstatusService = brevstatusService;
		this.brevtilgangService = brevtilgangService;
	}

	/**
	 * Forespørsel lagres i databasen. Deretter sendes den originale meldingen videre på definert kø.
	 */
	@Handler
	public void execute(Exchange exchange) throws BrevException {

		MessageVO messageVo = Utils.getMessageVoFromExchange(exchange);
		BrevStatusVO brevStatusVo = generateBrevStatusVo(messageVo);
		if (brevStatusVo == null) {
			throw new BrevFunctionalException("BrevStatus er null");
		}

		//Ved feil systempassord send en feilmelding tilbake til fagsystemet over riktig kø.
		//Setter bodyen til exchangen til feilmeldingen og ruter den til riktig kø
		if (!brevtilgangService.sjekkSystemTilgang(brevStatusVo.getSystemID(), brevStatusVo.getPassord())) {
			log.warn("Feil systempassord for melding fra " + brevStatusVo.getSystemID());
			String xmlKvittering = lagFeilmelding(Konstanter.FEIL_IKKE_SYSTEM_TILGANG, brevStatusVo);

			Utils.setBodyAndReturnQueue(exchange, xmlKvittering, messageVo.getReplyQueueName());
			return;
		}

		try {
			if (messageVo.isTilgangsXML()) {
				giTilgang(brevStatusVo);
			} else {
				bestillBrev(brevStatusVo);
			}
		} catch (BrevTechnicalException e) {
			log.error("Feil ved henting av brevstatus. " + e.getMessage());
		}
	}

	private void bestillBrev(BrevStatusVO brevStatusVo) throws BrevTechnicalException {
		BrevStatusVO brevEksisterer = brevstatusService.hentBrevStatus(brevStatusVo.getSystemID(),
				brevStatusVo.getBrevreferanse());

		if (brevEksisterer != null) {
			log.warn("Brevet eksisterer fra før " + brevStatusVo.getBrevreferanse());
			String feilmelding = lagFeilmelding(Konstanter.FEIL_BREV_EKSISTERER, brevStatusVo);
			//TODO: send feilmelding
			//producer.sendReturMelding(brevStatusVo.getReturKoe(), false, messageVo.getCorrelationID(), feilmelding);
		} else {
			brevStatusVo.setStatus(Konstanter.BREVSTATUS_BREVPAKKE);
			brevstatusService.lagreBrevStatus(brevStatusVo);
			//producer.sendToDialogue(messageVo);
			//log.info(methSig, "Brevet er sendt til bestilling/opprettelse i Dialogue");
		}
	}


	private void giTilgang(BrevStatusVO brevStatusVo) throws BrevTechnicalException {
		boolean ok = brevtilgangService.lagreTilgang(brevStatusVo.getSystemID(), brevStatusVo.getBrevreferanse(),
				brevStatusVo.getToken());
		if (ok) {
			log.debug("Token '" + brevStatusVo.getCensoredToken() + "' er satt for systemID '" + brevStatusVo.getSystemID()
					+ "' for brevreferanse '" + brevStatusVo.getBrevreferanse() + "'");
			log.info("Tilgang gitt for systemID '" + brevStatusVo.getSystemID() + "'");
		} else {
			log.warn("Kunne ikke gi tilgang '" + brevStatusVo.getCensoredToken() +
					"' for systemID '" + brevStatusVo.getSystemID() + "'");
		}
	}

	private BrevStatusVO generateBrevStatusVo(MessageVO messageVO) throws BrevTechnicalException {

		if (messageVO == null || messageVO.getStringBody() == null) {
			throw new BrevTechnicalException("Ugyldig XML: InputMessage er null");
		}
		StringReader reader = new StringReader(messageVO.getStringBody());
		BrevStatusVO brevStatusVo;

		try {
			brevStatusVo = XMLService.marshalBrevStatus(reader);
			messageVO.setTilgangsXML(Konstanter.BREVMODUS_FRALAGER.equals(brevStatusVo.getModus()));
		} catch (BrevTechnicalException e) {
			log.error("Ugyldig XML: " + messageVO.getStringBody());
			throw e;
		}

		brevStatusVo.setReturKoe(messageVO.getReplyQueueName());
		messageVO.setBrevreferanse(brevStatusVo.getBrevreferanse());

		if (!brevStatusVo.getSystemID().startsWith(SystemType.PE.toString())) {
			String errorMessage = "Brev med feil systemID mottatt: '" + brevStatusVo.getSystemID()
					+ "', forventer pensjonsbrev";
			throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, errorMessage, null);
		}

		try {
			notEmpty("Brevreferanse", brevStatusVo.getBrevreferanse(), false);
			notEmpty("Systemid", brevStatusVo.getSystemID(), false);
			notEmpty("Returkø", brevStatusVo.getReturKoe(), false);
		} catch (BrevException e) {
			log.error("Ugyldig XML mottatt for brevreferanse " + messageVO.getBrevreferanse(), e);
			throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, e);
		}
		return brevStatusVo;
	}

	/**
	 * Lager feilmelding basert på feiltype.
	 *
	 * @param feilType
	 * @return Feilmeldings-XML
	 */
	private String lagFeilmelding(String feilType, BrevStatusVO brevStatusVo) {
		KvitteringVO kvittering = new KvitteringVO();
		kvittering.setSystemID(brevStatusVo.getSystemID());
		kvittering.setBrevreferanse(brevStatusVo.getBrevreferanse());
		kvittering.setFeilkode(feilType);
		brevStatusVo.setStatus(Konstanter.BREVSTATUS_FEIL);

		String xmlKvittering = XMLService.unmarshal(kvittering, brevStatusVo);
		return xmlKvittering;
	}

}
