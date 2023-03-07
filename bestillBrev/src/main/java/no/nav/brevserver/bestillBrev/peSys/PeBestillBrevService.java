package no.nav.brevserver.bestillBrev.peSys;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.alias.BrevserverProperties;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
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

@Slf4j
@Component
public class PeBestillBrevService {

	private final Metrics metrics;
	private final BrevstatusService brevstatusService;
	private final BrevtilgangService brevtilgangService;
	private final BrevserverProperties brevserverProperties;


	public PeBestillBrevService(Metrics metrics, BrevstatusService brevstatusService,
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
			throw new BrevFunctionalException("BrevStatus er null");
		}

		if (messageVo.isTilgangsXML()) {
			boolean ok = brevtilgangService.lagreTilgang(brevStatusVo.getSystemID(), brevStatusVo.getBrevreferanse(),
					brevStatusVo.getToken());
			if (ok) {
				log.info("Tilgang gitt for systemID '" + brevStatusVo.getSystemID() + "' med brevref: " + brevStatusVo.getBrevreferanse());
			} else {
				log.warn("Kunne ikke gi tilgang '" + brevStatusVo.getCensoredToken()
						+ "' for systemID '" + brevStatusVo.getSystemID() + "' med brevref: " + brevStatusVo.getBrevreferanse());
			}
			exchange.setProperty(SENDTOMODE, INGEN_TILBAKEMELDING);

			// Bestill fra Dialogue
		} else {
			BrevStatusVO tmp = brevstatusService.hentBrevStatus(brevStatusVo.getSystemID(), brevStatusVo.getBrevreferanse());
			if (tmp != null) {
				log.warn("Brevet eksisterer fra før " + brevStatusVo.getBrevreferanse());
				setBodyAndReturnQueueWithMode(
						exchange,
						lagFeilmelding(Konstanter.FEIL_BREV_EKSISTERER, brevStatusVo),
						brevStatusVo.getReturKoe(),
						GI_FEILMELDING);
				return;
			}

			brevStatusVo.setStatus(Konstanter.BREVSTATUS_BREVPAKKE);
			if(brevStatusVo.getReturKoe() == null) {
				brevStatusVo.setReturKoe(messageVo.getReplyQueueName());
			}
			brevstatusService.lagreBrevStatus(brevStatusVo);
			log.info("Brev med brevref: " + brevStatusVo.getBrevreferanse() +" er arkivert i Brevlageret");

			if(brevserverProperties.isLoggXML()){
				log.info("Pensjons-XML til Exstream:\n" + messageVo.getStringBody());
			}
			setBodyAndMode(exchange,
					messageVo.getStringBody(),
					OPPRETT_BREV);

			metrics.incrementBrevkodeMetric(brevStatusVo.getSystemID(), brevStatusVo.getBrevmal());
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
		} catch (BrevTechnicalException e) {
			log.warn("Ugyldig XML mottatt");
			throw e;
		}
		messageVO.setTilgangsXML(Konstanter.BREVMODUS_FRALAGER.equals(brevStatusVo.getModus()));
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
}
