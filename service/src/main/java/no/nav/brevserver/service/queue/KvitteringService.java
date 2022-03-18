package no.nav.brevserver.service.queue;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.utils.ExchangeUtils;
import no.nav.brevserver.core.utils.xmlHandlers.XMLService;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.KvitteringVO;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import static no.nav.brevserver.core.mdc.MDCConstants.MDC_CALL_ID;
import static no.nav.brevserver.core.utils.ExchangeUtils.overrideDestination;
import static no.nav.brevserver.service.queue.KvitteringRoute.DIRECT_SENDKVITTERINGROUTE;

@Service
@Slf4j
public class KvitteringService {

	private final ProducerTemplate producerTemplate;
	private final CamelContext context;


	public KvitteringService(ProducerTemplate producerTemplate, CamelContext context) {
		this.producerTemplate = producerTemplate;
		this.context = context;
	}

	public void sendKvittering(BrevVO brev, BrevStatusVO brevstatus, SystemType systemType, String returKoe){
		log.info("Sender kvittering, brevStatus: " + brev.getLagerStatus());

		KvitteringVO kvittering = createKvittering(brevstatus, brev);
		String xmlKvittering = XMLService.unmarshal(kvittering, brevstatus);

		doSendKvittering(xmlKvittering, returKoe);
	}

	public void sendKvittering(String xmlKvittering, String returKoe){

		doSendKvittering(xmlKvittering, returKoe);
	}

	private void doSendKvittering(String xmlKvittering, String returKoe) {
		try {
			ExchangeBuilder exchangeBuilder = new ExchangeBuilder(context);
			Exchange kvitteringExchange = exchangeBuilder.withBody(xmlKvittering).build();
			overrideDestination(kvitteringExchange, returKoe);
			MDC.put(MDC_CALL_ID, kvitteringExchange.getExchangeId());

			producerTemplate.send(DIRECT_SENDKVITTERINGROUTE, kvitteringExchange);
		} catch (Exception e) {
			log.error("Klarte ikke sende melding: " + e.getMessage() + " \n" + e.getStackTrace());
		}
	}

	private KvitteringVO createKvittering(BrevStatusVO brevstatus, BrevVO brev) {
		KvitteringVO kvittering = new KvitteringVO();
		kvittering.setBrevreferanse(brevstatus.getBrevreferanse());
		kvittering.setSystemID(brevstatus.getSystemID());
		kvittering.setLagerStatus(brevstatus.getStatus());
		kvittering.setContentType(brev.getContentType());
		return kvittering;
	}

}
