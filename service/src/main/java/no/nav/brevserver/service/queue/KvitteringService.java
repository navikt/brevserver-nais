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
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Queue;

import static no.nav.brevserver.core.utils.ExchangeUtils.buildReturnQueue;
import static no.nav.brevserver.core.utils.ExchangeUtils.setDestination;
import static no.nav.brevserver.service.queue.KvitteringRoute.DIRECT_SENDKVITTERINGROUTE;
import static org.apache.logging.log4j.util.Strings.isEmpty;

@Component
@Slf4j
public class KvitteringService {

	private static final String JMSCORRELATIONID = "JMSCorrelationID";
	private final ProducerTemplate producerTemplate;
	private final CamelContext context;
	private final Queue brevReplyPe;


	public KvitteringService(ProducerTemplate producerTemplate, CamelContext context, Queue brevReplyPe) {
		this.producerTemplate = producerTemplate;
		this.context = context;
		this.brevReplyPe = brevReplyPe;
	}

	public void sendKvittering(BrevVO brev, BrevStatusVO brevstatus, SystemType systemType, String returKoe) {
		log.info("Sender kvittering for brevref: " + brev.getBrevreferanse() + ", brevStatus: " + brev.getLagerStatus());

		KvitteringVO kvittering = createKvittering(brevstatus, brev);
		String xmlKvittering = XMLService.unmarshal(kvittering, brevstatus);

		if (SystemType.PE.equals(systemType) && (returKoe == null || isEmpty(returKoe.trim()))) {
			try {
				returKoe = brevReplyPe.getQueueName();
			} catch (JMSException exception) {
				log.error("Klarte ikke hente kønavn. Avbryter kvitteringen.");
				return;
			}
		}

		doSendKvittering(xmlKvittering, buildReturnQueue(returKoe));
	}

	/*
	 * Elin / predator leter etter kvitteringsmeldinger basert på correlationID'en; Legger den på her.
	 */
	public void sendKvitteringBiMedCorrelationID(String xmlKvittering, String returKoe, Exchange exchange) {

		String correlationID = (String) exchange.getIn().getHeader(JMSCORRELATIONID);
		doSendKvitteringMedCorrelationID(xmlKvittering, returKoe, correlationID);
	}

	public void sendKvitteringBi(String xmlKvittering, String returKoe) {

		doSendKvittering(xmlKvittering, returKoe);
	}

	private void doSendKvittering(String xmlKvittering, String returKoe) {
		doSendKvitteringMedCorrelationID(xmlKvittering, returKoe, null);
	}

	private void doSendKvitteringMedCorrelationID(String xmlKvittering, String returKoe, String correlationID) {
		try {

			if (returKoe != null) {
				returKoe = returKoe.trim();
			}

			if (isEmpty(returKoe)) {
				log.warn("Ingen returkø er definert. Avbryter kvitteringsløpet.");
				return;
			}
			ExchangeBuilder exchangeBuilder = new ExchangeBuilder(context);
			Exchange kvitteringExchange = exchangeBuilder.withBody(xmlKvittering).build();
			setDestination(kvitteringExchange, buildReturnQueue(returKoe));

			kvitteringExchange.getIn().setHeader("JMS_IBM_Format", "MQSTR");
			if (!isEmpty(correlationID)) {
				kvitteringExchange.getIn().setHeader(JMSCORRELATIONID, correlationID);
			}

			log.info("Brevserver leverer kvitteringen til: " + returKoe + " med correlationID: " + correlationID);
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
