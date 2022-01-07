package no.nav.brevserver.arkiverBrev;

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
public class PeArkiverBrevRoute extends RouteBuilder {
	public static final String PE_ARKIVER_BREV_ROUTE = "direct:peArkiverBrev";
	private final String ROUTE_OPTIONS = "?transacted=true&concurrentConsumers=1";//&mapJmsMessage=false";


	private final Queue mottakArkivPe;
	private final Queue mottakOnlinePe;
	private final Queue deadletterPe;
	private final ArkiverBrevMetricsRoutePolicy arkiverBrevMetricsRoutePolicy;
	private final PeArkiverBrevService peArkiverBrevService;


	@Inject
	public PeArkiverBrevRoute(Queue mottakArkivPe,
							  Queue mottakOnlinePe,
							  Queue deadletterPe,
							  ArkiverBrevMetricsRoutePolicy arkiverBrevMetricsRoutePolicy,
							  PeArkiverBrevService peArkiverBrevService) {
		this.mottakArkivPe = mottakArkivPe;
		this.mottakOnlinePe = mottakOnlinePe;
		this.deadletterPe = deadletterPe;
		this.arkiverBrevMetricsRoutePolicy = arkiverBrevMetricsRoutePolicy;
		this.peArkiverBrevService = peArkiverBrevService;
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
				.log(LoggingLevel.WARN, "DetailedJMSException oppstått i arkiverBrev for forsendelse med  getIdsForLogging() . Melding sendt til funksjonell feilkø.")
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(false)
				.handled(true)
				.to("jms:" + deadletterPe.getQueueName());

		/*from("jms:" + mottakArkivPe.getQueueName() + ROUTE_OPTIONS)
				.log(INFO, log, "mottat melding fra mq")
				.to(PE_ARKIVER_BREV_ROUTE);
		from("jms:" + mottakOnlinePe.getQueueName() + ROUTE_OPTIONS)
				.log(INFO, log, "mottat melding fra mq")
				.to(PE_ARKIVER_BREV_ROUTE);*/

		from("file://C:/Users/b157935/Documents/brevserverTest/?filename=nyPeTest.txt&charset=ISO-8859-1")
			.convertBodyTo(String.class)
			.to(PE_ARKIVER_BREV_ROUTE);

		//Hent svar fra exstream
		from(PE_ARKIVER_BREV_ROUTE)
				.routeId(PE_ARKIVER_BREV_ROUTE)
				.routePolicy(arkiverBrevMetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.log(LoggingLevel.INFO, log, PE_ARKIVER_BREV_ROUTE + " starter behandlingen")
				.bean(peArkiverBrevService)
				.toD("jms:${header.uri}")
				.log(LoggingLevel.INFO, log, "Kvitteringsmeldingen er sendt til: " + "${header.uri}")
				.end();


	}
}
