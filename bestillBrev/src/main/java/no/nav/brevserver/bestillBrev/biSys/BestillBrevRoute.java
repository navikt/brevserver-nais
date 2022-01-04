package no.nav.brevserver.bestillBrev.biSys;

import com.ibm.msg.client.jms.DetailedJMSException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.Queue;

import static no.nav.brevserver.core.utils.mqUtils.Utils.RETURNQUEUE;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;

@Component
public class BestillBrevRoute extends RouteBuilder {
	public static final String BESTILL_BREV_ROUTE = "direct:bestillBrev";
	public static final String BESTILLBREV = "bestill_brev";
	private final String ROUTE_OPTIONS = "?transacted=true&concurrentConsumers=1";//&mapJmsMessage=false";
	public static String MODE_OPPRETT_BREV = "OPPRETT_BREV";
	public static String MODE_LAGRE_TILGANG = "LAGRE_TILGANG";
	public static String MODE_RETURN_FEILMELDING = "FEILSITUASJON";
	public static String HEADER_SENDTOMODE = "SENDTOQUEUE";
	private static String HEADER_SENDTOQUEUE = "header.SENDTOQUEUE";

	private final Queue onlinebrev;
	private final Queue dialogueOnline;
	private final Queue deadletter;
	private final BestillBrevMetricsRoutePolicy bestillBrevMetricsRoutePolicy;
	private final BestillBrevService bestillBrevService;

	@Inject
	public BestillBrevRoute(Queue onlinebrev,
							Queue deadletter,
							Queue dialogueOnline,
							BestillBrevMetricsRoutePolicy arkiverBrevMetricsRoutePolicy,
							BestillBrevService arkiverBrevService) {
		this.onlinebrev = onlinebrev;
		this.deadletter = deadletter;
		this.dialogueOnline = dialogueOnline;
		this.bestillBrevMetricsRoutePolicy = arkiverBrevMetricsRoutePolicy;
		this.bestillBrevService = arkiverBrevService;
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
				.log(LoggingLevel.WARN, log, "${exception}; ")
				.to("jms:" + deadletter.getQueueName());


		onException(BrevTechnicalException.class)
				.handled(true)
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.log(ERROR, log, "${exception}; ")
				.to("jms:" + deadletter.getQueueName());


		onException(DetailedJMSException.class)
				.log(LoggingLevel.WARN, "DetailedJMSException oppstått i bestillBrev for forsendelse med  getIdsForLogging() . Melding sendt til funksjonell feilkø.")
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(false)
				.handled(true)
				.to("jms:" + deadletter.getQueueName());


		/*from("jms:" + onlinebrev.getQueueName() + ROUTE_OPTIONS)
				.log(INFO, log, "mottat melding fra mq")
				.to(BESTILL_BREV_ROUTE);*/

		from("file://C:/Users/b157935/Documents/brevserverTest/?filename=test2.txt&charset=ISO-8859-1")
				.convertBodyTo(String.class)
				.to(BESTILL_BREV_ROUTE);

		//Brevbestilling fra Bisys
		from(BESTILL_BREV_ROUTE)
				.routeId(BESTILLBREV)
				.routePolicy(bestillBrevMetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.log(INFO, log, BESTILLBREV + " starter behandlingen")
				.bean(bestillBrevService)
				.process(
						exchange -> {
							System.out.println("noe");
						}
				)
				.choice()
				.when(simple("${" + HEADER_SENDTOQUEUE +"} == '" + MODE_OPPRETT_BREV + "'"))
				//.to("direct:soppel")
				.to("jms:" + dialogueOnline.getQueueName())
				.when(simple("${" + HEADER_SENDTOQUEUE +"} == '" + MODE_LAGRE_TILGANG + "'"))
				//noop, melding fra brevklient
				.when(simple("${" + HEADER_SENDTOQUEUE +"} == '" + MODE_RETURN_FEILMELDING+"'"))
				.process(exchange -> {
					if (exchange.getProperty(RETURNQUEUE) == null)
						exchange.setProperty(RETURNQUEUE, deadletter.getQueueName());
				})
				.to("jms:{header." + RETURNQUEUE + "}")
				.otherwise()
				.to("jms:" + deadletter.getQueueName())
				.end();

		from("direct:soppel")
				.process(
						exchange -> {
							System.out.println("noe");
						}
				)
				.end();

	}
}

