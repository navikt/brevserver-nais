package no.nav.brevserver.bestillBrev.bestillBrevDefault;

import com.ibm.msg.client.jms.DetailedJMSException;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.Queue;

import static org.apache.camel.LoggingLevel.ERROR;

@Component
public class BestillBrevRoute extends RouteBuilder {
	public static final String BESTILLBREV = "bestill_brev";
	private final String ROUTE_OPTIONS = "?transacted=true&concurrentConsumers=1&mapJmsMessage=false";

	private final Queue onlinebrev;
	private final Queue deadletter;
	private final BestillBrevMetricsRoutePolicy bestillBrevMetricsRoutePolicy;
	private final BestillBrevService bestillBrevService;

	@Inject
	public BestillBrevRoute(Queue onlinebrev,
							Queue deadletter,
							BestillBrevMetricsRoutePolicy arkiverBrevMetricsRoutePolicy,
							BestillBrevService arkiverBrevService) {
		this.onlinebrev = onlinebrev;
		this.deadletter = deadletter;
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

		onException(DetailedJMSException.class)
				.log(LoggingLevel.WARN, "DetailedJMSException oppstått i bestillBrev for forsendelse med  getIdsForLogging() . Melding sendt til funksjonell feilkø.")
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(false)
				.handled(true)
				.to("jms:" + deadletter.getQueueName());


		from("jms:" + onlinebrev.getQueueName() + ROUTE_OPTIONS)
				.routeId(BESTILLBREV)
				.routePolicy(bestillBrevMetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.log(LoggingLevel.INFO, log, BESTILLBREV + " starter behandlingen")
				.bean(bestillBrevService)
				//.to(dialogOnlineQueue)
				.toD("jms:${header.uri}")
				.log(LoggingLevel.INFO, log, "Kvitteringsmeldingen er sendt til: " + "${header.uri}")
				.end();


	}
}
