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

import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;

@Component
public class PeBestillBrevRoute extends RouteBuilder {
	public static final String BESTILLBREV = "peBestill_brev";
	private final String ROUTE_OPTIONS = "?transacted=true&concurrentConsumers=1";//&mapJmsMessage=false";
	public static final String BESTILL_BREV_ROUTE_PE = "direct:bestillBrevPe";

	private final Queue onlinebrevPe;
	private final Queue dialogueOnlinePe;
	private final Queue deadletterPe;
	private final BestillBrevMetricsRoutePolicy bestillBrevMetricsRoutePolicy;
	private final PeBestillBrevService peBestillBrevService;

	@Inject
	public PeBestillBrevRoute(Queue onlinebrevPe,
							Queue deadletterPe,
							Queue dialogueOnlinePe,
							//TODO: PeBestillBrevMetrics? Unødvendig? Undersøk!
							BestillBrevMetricsRoutePolicy peBestillBrevMetricsRoutePolicy,
							PeBestillBrevService peBestillBrevService) {
		this.onlinebrevPe = onlinebrevPe;
		this.deadletterPe = deadletterPe;
		this.dialogueOnlinePe = dialogueOnlinePe;
		this.bestillBrevMetricsRoutePolicy = peBestillBrevMetricsRoutePolicy;
		this.peBestillBrevService = peBestillBrevService;
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
				.to("jms:" + deadletterPe.getQueueName());


		onException(BrevTechnicalException.class)
				.handled(true)
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.log(ERROR, log, "${exception}; ")
				.to("jms:" + deadletterPe.getQueueName());


		onException(DetailedJMSException.class)
				.log(LoggingLevel.WARN, "DetailedJMSException oppstått i bestillBrev for forsendelse med  getIdsForLogging() . Melding sendt til funksjonell feilkø.")
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(false)
				.handled(true)
				.to("jms:" + deadletterPe.getQueueName());


		/*from("file://C:/Users/b157935/Documents/brevserverTest/?filename=peTest.txt&charset=ISO-8859-1")
				.convertBodyTo(String.class)
				.to(BESTILL_BREV_ROUTE_PE);*/
/*
		from("jms:" + onlinebrevPe.getQueueName() + ROUTE_OPTIONS)
				.log(INFO, log, "mottat melding fra mq")
				.to(BESTILL_BREV_ROUTE_PE);
*/
		//Brevbestilling fra Pesys
		from(BESTILL_BREV_ROUTE_PE)
				.routeId(BESTILLBREV)
				.routePolicy(bestillBrevMetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.log(LoggingLevel.INFO, log, BESTILLBREV + " starter behandlingen")
				.bean(peBestillBrevService)
				.to("jms:" + dialogueOnlinePe.getQueueName())
				.log(LoggingLevel.INFO, log, "Kvitteringsmeldingen er sendt til: " + "${header.uri}")
				.end();

	}
}
