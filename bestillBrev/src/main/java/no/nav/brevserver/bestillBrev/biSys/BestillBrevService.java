package no.nav.brevserver.bestillBrev.biSys;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.properties.BrevserverProperties;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.metrics.Metrics;
import no.nav.brevserver.core.utils.ExchangeUtils;
import no.nav.brevserver.core.utils.xmlHandlers.XMLService;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.MessageVO;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.io.StringReader;

import static no.nav.brevserver.bestillBrev.utils.Utils.lagFeilmelding;
import static no.nav.brevserver.core.utils.ExchangeUtils.SENDTOMODE;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_FEILMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.INGEN_TILBAKEMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.OPPRETT_BREV;
import static no.nav.brevserver.core.utils.ExchangeUtils.notEmpty;
import static no.nav.brevserver.core.utils.ExchangeUtils.setBodyAndMode;
import static no.nav.brevserver.core.utils.ExchangeUtils.setBodyAndReturnQueueWithMode;
import static no.nav.brevserver.core.utils.SafeLoggingUtil.sanitizeUnsafeChar;


/**
 * Håndterer meldinger fra Dialogue. Lagrer brev og setter status og sender kvittering til saksbehandlingsystemet.
 */
@Slf4j
@Component
public class BestillBrevService {

	private final Metrics metrics;
	private final BrevstatusService brevstatusService;
	private final BrevtilgangService brevtilgangService;
	private final BrevserverProperties brevserverProperties;

	public BestillBrevService(Metrics metrics,
							  BrevstatusService brevstatusService,
							  BrevtilgangService brevtilgangService,
							  BrevserverProperties brevserverProperties) {
		this.metrics = metrics;
		this.brevstatusService = brevstatusService;
		this.brevtilgangService = brevtilgangService;
		this.brevserverProperties = brevserverProperties;
	}

	/**
	 * Forespørsel lagres i databasen. Deretter sendes den originale meldingen videre på definert kø.
	 */
	@Handler
	public void execute(Exchange exchange) throws BrevException {
		MessageVO messageVo = ExchangeUtils.getMessageVoFromExchange(exchange);
		BrevStatusVO brevStatusVo = generateBrevStatusVo(messageVo);

		if (brevStatusVo == null) {
			throw new BrevFunctionalException("BrevStatusVo er null");
		}
		// Hvis modus="frabrevlager" ønsker et fagsystem å gi en tilgang til brevet fra brevklient med en token
		if (brevStatusVo.getModus() != null && Konstanter.BREVMODUS_FRALAGER.equals(brevStatusVo.getModus())) {
			brevStatusVo.setReturKoe("ko");
			boolean ok = brevtilgangService.lagreTilgang(brevStatusVo.getSystemID(), brevStatusVo.getBrevreferanse(),
					brevStatusVo.getToken());
			if (ok) {
				log.info("Tilgang gitt for systemID '{}' med brevref:{}", sanitizeUnsafeChar(brevStatusVo.getSystemID()), sanitizeUnsafeChar(brevStatusVo.getBrevreferanse()));
			} else {
				log.warn("Kunne ikke gi tilgang til systemID='{}' med brevref={}",
						sanitizeUnsafeChar(brevStatusVo.getSystemID()), sanitizeUnsafeChar(brevStatusVo.getBrevreferanse()));
			}
			exchange.setProperty(SENDTOMODE, INGEN_TILBAKEMELDING);
		} else {
			// Bestill fra Dialogue
			BrevStatusVO tmp = brevstatusService.hentBrevStatus(brevStatusVo.getBrevreferanse(), brevStatusVo.getSystemID());
			if (tmp != null) {
				log.warn("Brevet eksisterer fra før {}", sanitizeUnsafeChar(brevStatusVo.getBrevreferanse()));
				setBodyAndReturnQueueWithMode(
						exchange,
						lagFeilmelding(Konstanter.FEIL_BREV_EKSISTERER, brevStatusVo),
						brevStatusVo.getReturKoe(),
						GI_FEILMELDING
				);
			} else {
				brevStatusVo.setStatus(Konstanter.BREVSTATUS_BREVPAKKE);
				brevStatusVo.setReturKoe(messageVo.getReplyQueueName());
				brevstatusService.lagreBrevStatus(brevStatusVo);

				log.info("Brev med brevref:{} er arkivert i Brevlageret", sanitizeUnsafeChar(brevStatusVo.getBrevreferanse()));
				if (brevserverProperties.isLoggXML()) {
					log.info("Bidrags-XML til Exstream:\n" + messageVo.getStringBody());
				}
				setBodyAndMode(exchange,
						messageVo.getStringBody(),
						OPPRETT_BREV);

				metrics.incrementBrevkodeMetric(brevStatusVo.getSystemID(), brevStatusVo.getBrevmal());
			}
		}
	}

	private BrevStatusVO generateBrevStatusVo(MessageVO messageVO) throws BrevFunctionalException {
		if (messageVO == null || messageVO.getStringBody() == null) {
			throw new BrevFunctionalException("Ugyldig XML: InputMessage er null");
		}
		StringReader reader = new StringReader(messageVO.getStringBody());
		BrevStatusVO brevStatusVo;

		try {
			brevStatusVo = XMLService.marshalBrevStatus(reader);
		} catch (Exception e) {
			throw new BrevFunctionalException("Ugyldig XML mottatt, feilmelding: " + e.getMessage());
		}

		messageVO.setTilgangsXML(brevStatusVo != null && Konstanter.BREVMODUS_FRALAGER.equals(brevStatusVo.getModus()));
		brevStatusVo.setReturKoe(messageVO.getReplyQueueName());
		messageVO.setBrevreferanse(brevStatusVo.getBrevreferanse());

		if (brevStatusVo.getSystemID().startsWith(SystemType.PE.toString())) {
			String errorMessage = "Brev med feil systemID mottatt: '" + brevStatusVo.getSystemID()
					+ "', forventet ikke pensjonsbrev";
			throw new BrevFunctionalException(errorMessage, null);
		}

		try {
			notEmpty("Brevreferanse", brevStatusVo.getBrevreferanse(), false);
			notEmpty("Systemid", brevStatusVo.getSystemID(), false);
			notEmpty("Returkø", brevStatusVo.getReturKoe(), false);
		} catch (BrevException e) {
			String errorMsg = "Ugyldig XML mottatt for brevreferanse " + messageVO.getBrevreferanse();
			log.warn(errorMsg, e);
			throw new BrevFunctionalException(errorMsg, e);
		}
		return brevStatusVo;
	}

}


