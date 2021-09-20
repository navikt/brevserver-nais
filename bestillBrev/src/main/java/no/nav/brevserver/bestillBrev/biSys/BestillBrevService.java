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

import static no.nav.brevserver.core.utils.mqUtils.Utils.URI;
import static no.nav.brevserver.core.utils.mqUtils.Utils.notEmpty_old;


/**
 * Håndterer meldinger fra Dialogue. Lagrer brev og setter status og sender kvittering til saksbehandlingsystemet.
 *
 * @author Holger Zobel, Accenture
 */
@Slf4j
@Component
public class BestillBrevService {

	private final BrevstatusService brevstatusService;
	private final BrevtilgangService brevtilgangService;


	public BestillBrevService(BrevstatusService brevstatusService,
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
		if(brevStatusVo == null){

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

		// Hvis modus="frabrevlager" ønsker et fagsystem å gi en tilgang til brevet fra brevklient med en token
		if (brevStatusVo.getModus() != null && Konstanter.BREVMODUS_FRALAGER.equals(brevStatusVo.getModus())) {
			// Lagre token
			boolean ok = brevtilgangService.lagreTilgang(brevStatusVo.getSystemID(), brevStatusVo.getBrevreferanse(),
					brevStatusVo.getToken());
			if (ok) {
				log.info("Tilgang gitt for systemID '" + brevStatusVo.getSystemID() + "'");
			} else {
				log.warn("Kunne ikke gi tilgang '" + brevStatusVo.getCensoredToken()
						+ "' for systemID '" + brevStatusVo.getSystemID() + "'");
			}
			// Bestille brevet fra Dialogue
		} else {
			BrevStatusVO tmp = brevstatusService.hentBrevStatus(brevStatusVo.getSystemID(), brevStatusVo.getBrevreferanse());
			if (tmp != null) {
				// Brevet eksisterer fra før, returner feilmelding
				log.warn("Brevet eksisterer fra før " + brevStatusVo.getBrevreferanse());
				String xmlKvittering = lagFeilmelding(Konstanter.FEIL_BREV_EKSISTERER, brevStatusVo);
				Utils.setBodyAndReturnQueue(exchange, xmlKvittering, brevStatusVo.getReturKoe());
				return;
			}

			brevStatusVo.setStatus(Konstanter.BREVSTATUS_BREVPAKKE);
			brevstatusService.lagreBrevStatus(brevStatusVo);
			exchange.getIn().setBody(messageVo.getStringBody());
			exchange.getIn().setHeader(URI, messageVo.getReplyQueueName());
		}
	}

	private BrevStatusVO generateBrevStatusVo(MessageVO messageVO) throws BrevTechnicalException {

		StringReader reader = new StringReader(messageVO.getStringBody());
		BrevStatusVO brevStatusVo;

		try {
			brevStatusVo = XMLService.marshalBrevStatus(reader);
			messageVO.setTilgangsXML(brevStatusVo != null && Konstanter.BREVMODUS_FRALAGER.equals(brevStatusVo.getModus()));
		} catch (BrevTechnicalException e) {
			log.error("Ugyldig XML: " + messageVO.getStringBody());
			throw e;
		}

		brevStatusVo.setReturKoe(messageVO.getReplyQueueName());
		messageVO.setBrevreferanse(brevStatusVo.getBrevreferanse());

		if (brevStatusVo.getSystemID().startsWith(SystemType.PE.toString())) {
			String errorMessage = "Brev med feil systemID mottatt: '" + brevStatusVo.getSystemID()
					+ "', forventet ikke pensjonsbrev";
			log.error("BestillBrevCommand.validate()", errorMessage);
			throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, errorMessage, null);
		}

		try {
			notEmpty_old("Brevreferanse", brevStatusVo.getBrevreferanse(), false);
			notEmpty_old("Systemid", brevStatusVo.getSystemID(), false);
			notEmpty_old("Returkø", brevStatusVo.getReturKoe(), false);
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


