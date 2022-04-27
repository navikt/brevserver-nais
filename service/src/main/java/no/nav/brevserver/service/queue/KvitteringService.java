package no.nav.brevserver.service.queue;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.utils.xmlHandlers.XMLService;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.KvitteringVO;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Queue;

import static no.nav.brevserver.core.utils.ExchangeUtils.buildReturnQueue;
import static no.nav.brevserver.core.utils.ExchangeUtils.setDestination;
import static no.nav.brevserver.core.utils.ExchangeUtils.setDestinationWithQueueString;
import static no.nav.brevserver.service.queue.KvitteringRoute.DIRECT_SENDKVITTERINGROUTE;

@Component
@Slf4j
public class KvitteringService {

	private final ProducerTemplate producerTemplate;
	private final CamelContext context;
	private final Queue brevReplyPe;


	public KvitteringService(ProducerTemplate producerTemplate, CamelContext context, Queue brevReplyPe) {
		this.producerTemplate = producerTemplate;
		this.context = context;
		this.brevReplyPe = brevReplyPe;
	}

	public void sendKvittering(BrevVO brev, BrevStatusVO brevstatus, SystemType systemType, String returKoe) {
		log.info("Sender kvittering, brevStatus: " + brev.getLagerStatus());

		KvitteringVO kvittering = createKvittering(brevstatus, brev);
		String xmlKvittering = XMLService.unmarshal(kvittering, brevstatus);

		if(StringUtils.isEmpty(returKoe)){
			if( SystemType.PE.equals(systemType)) {
				try {
					returKoe = brevReplyPe.getQueueName();
				} catch (JMSException exception) {
					log.error("Klarte ikke hente kønavn. Avbryter kvitteringen.");
					return;
				}
			} else {
				log.warn("ReturKo er tom. Sender ikke kvittering");
			}
		}

		doSendKvittering(xmlKvittering, buildReturnQueue(returKoe));
	}

	public void sendKvitteringBi(String xmlKvittering, String returKoe){

		doSendKvittering(xmlKvittering, returKoe);
	}

	private void doSendKvittering(String xmlKvittering, String returKoe) {
		try {
			ExchangeBuilder exchangeBuilder = new ExchangeBuilder(context);
			Exchange kvitteringExchange = exchangeBuilder.withBody(xmlKvittering).build();
			setDestination(kvitteringExchange, buildReturnQueue(returKoe));

			kvitteringExchange.getIn().setHeader("JMS_IBM_Format", "MQSTR");

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
