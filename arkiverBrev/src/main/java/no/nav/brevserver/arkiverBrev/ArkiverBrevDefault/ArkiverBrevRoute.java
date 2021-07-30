package no.nav.brevserver.arkiverBrev.ArkiverBrevDefault;

import com.ibm.msg.client.jms.DetailedJMSException;
import no.nav.brevserver.service.queue.xml.beans.XMLHandler;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.Queue;

import static org.apache.camel.LoggingLevel.ERROR;

@Component
public class ArkiverBrevRoute extends RouteBuilder {
	public static final String ARKIVER_BREV_ROUTE = "direct:arkiverBrev";


	private final Queue mottakArkiv;
	private final Queue mottakOnline;
	private final Queue deadletter;
	private ArkiverBrevMetricsRoutePolicy arkiverBrevMetricsRoutePolicy;
	private final MessageVoMapper messageVoMapper;
	private final ArkiverBrevService arkiverBrevService;


	@Inject
	public ArkiverBrevRoute(Queue mottakArkiv,
							Queue mottakOnline,
							Queue deadletter,
							ArkiverBrevMetricsRoutePolicy arkiverBrevMetricsRoutePolicy,
							MessageVoMapper messageVoMapper,
							ArkiverBrevService arkiverBrevService) {
		this.mottakArkiv = mottakArkiv;
		this.mottakOnline = mottakOnline;
		this.deadletter = deadletter;
		this.arkiverBrevMetricsRoutePolicy = arkiverBrevMetricsRoutePolicy;
		this.messageVoMapper = messageVoMapper;
		this.arkiverBrevService = arkiverBrevService;
	}

	@Override
	public void configure() throws Exception {
		errorHandler(defaultErrorHandler()
				.maximumRedeliveries(0)
				.log(log)
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(true)
				.loggingLevel(ERROR));

		onException(ValidationException.class)
				.handled(true)
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				//TODO: add logging
				//.log(LoggingLevel.WARN, log, "${exception}; " )
				.to("jms:" + deadletter.getQueueName());

		onException(DetailedJMSException.class)
				.log(LoggingLevel.WARN, "DetailedJMSException oppstått i bestillBrev for forsendelse med  getIdsForLogging() . Melding sendt til funksjonell feilkø.")
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(false)
				.handled(true)
				.to("jms:" + deadletter.getQueueName());


		from("jms:" + mottakArkiv.getQueueName() + "?transacted=true&concurrentConsumers=1")
				.to(ARKIVER_BREV_ROUTE);
		from("jms:" + mottakOnline.getQueueName() + "?transacted=true&concurrentConsumers=1")
				.to(ARKIVER_BREV_ROUTE);

		from(ARKIVER_BREV_ROUTE)
				.process(exchange -> {
					System.out.println("test");
				})
				.routeId(ARKIVER_BREV_ROUTE)
				.routePolicy(arkiverBrevMetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.log(LoggingLevel.INFO, log, ARKIVER_BREV_ROUTE + " starter behandling av en mq melding")
				.bean(messageVoMapper)
				.bean(arkiverBrevService)
				.to("jms:" + deadletter.getQueueName());
		//TODO: Trenger vi denne?
		//.process(new IdsProcessor())

		//.to("stax:no.nav.brevserver.arkiverBrev.XMLHandler")
				/*.process(new Processor(){
					public void process(Exchange exchange) throws Exception {
						XMLHandler handler = exchange.getIn().getBody(XMLHandler.class);
						handler.
				}*/
				//LoggID'er + logForsendelseId())
				//TODO: xsd for brevserver? ETter hvert?
				//.to("validator:no/nav/meldinger/virksomhet/dokdistfordeling/xsd/qdist008/out/distribuertilkanal.xsd")
				//TODO: kan nok ikke kjøre en default unmarshal når formatene er så rare
				//.unmarshal(new JaxbDataFormat(JAXBContext.newInstance(DistribuerTilKanal.class)))
				/*.bean(distribuerForsendelseTilDpiMapper)
				.bean(qdist011Service)
				.log(LoggingLevel.INFO, log, "qdist011 har sendt forsendelse med " + getIdsForLogging() + " til DPI")
				.bean(dokdistAdministrerForsendelseUpdater, "updateStatusAndConversationId")
				.log();*/

	}
}
