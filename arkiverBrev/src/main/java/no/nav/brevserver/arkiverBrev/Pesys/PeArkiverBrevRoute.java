package no.nav.brevserver.arkiverBrev.Pesys;

import com.ibm.msg.client.jms.DetailedInvalidDestinationException;
import com.ibm.msg.client.jms.DetailedJMSException;
import no.nav.brevserver.arkiverBrev.ArkiverBrevMetricsRoutePolicy;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.Queue;

import static no.nav.brevserver.core.utils.ExchangeUtils.DEFAULT_RETURN_QUEUE;
import static no.nav.brevserver.core.utils.ExchangeUtils.JMS;
import static no.nav.brevserver.core.utils.ExchangeUtils.JMS_OVERRIDDEN;
import static no.nav.brevserver.core.utils.ExchangeUtils.OVERRIDE_DESTINATION;
import static no.nav.brevserver.core.utils.ExchangeUtils.SENDTOMODE;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_FEILMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_TILBAKEMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.setDefaultReturnQueue;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;

@Component
public class PeArkiverBrevRoute extends RouteBuilder {
	public static final String PE_ARKIVER_BREV_ROUTE = "direct:peArkiverBrev";
	private final String ROUTE_OPTIONS = "?transacted=true&concurrentConsumers=1";//&mapJmsMessage=false";


	private final Queue mottakArkivPe;
	private final Queue mottakOnlinePe;
	private final Queue deadletterPe;
	private final Queue brevReplyPe;
	private final ArkiverBrevMetricsRoutePolicy arkiverBrevMetricsRoutePolicy;
	private final PeArkiverBrevService peArkiverBrevService;


	@Inject
	public PeArkiverBrevRoute(Queue mottakArkivPe,
							  Queue mottakOnlinePe,
							  Queue deadletterPe,
							  Queue brevReplyPe, ArkiverBrevMetricsRoutePolicy arkiverBrevMetricsRoutePolicy,
							  PeArkiverBrevService peArkiverBrevService) {
		this.mottakArkivPe = mottakArkivPe;
		this.mottakOnlinePe = mottakOnlinePe;
		this.deadletterPe = deadletterPe;
		this.brevReplyPe = brevReplyPe;
		this.arkiverBrevMetricsRoutePolicy = arkiverBrevMetricsRoutePolicy;
		this.peArkiverBrevService = peArkiverBrevService;
	}

	@Override
	public void configure() throws Exception {
		//@formatter:off
		errorHandler(defaultErrorHandler()
				.maximumRedeliveries(0)
				.log(log)
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(true)
				.loggingLevel(ERROR));

		onException(DetailedInvalidDestinationException.class)
				.handled(true)
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.log(LoggingLevel.WARN, log, "${exception}; ")
				.to(JMS + deadletterPe.getQueueName());


		onException(ValidationException.class)
				.handled(true)
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.log(LoggingLevel.WARN, log, "${exception}; ")
				.to(JMS + deadletterPe.getQueueName());

		onException(BrevTechnicalException.class)
				.handled(true)
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.log(ERROR, log, "${exception}; ")
				.to(JMS + deadletterPe.getQueueName());


		onException(DetailedJMSException.class)
				.log(LoggingLevel.WARN, "DetailedJMSException oppstått i PeArkiverBrevRoute ${exception}")
				.useOriginalMessage()
				.logExhaustedMessageBody(true)
				.logExhaustedMessageHistory(true)
				.logStackTrace(true)
				.handled(true)
				.to(JMS + deadletterPe.getQueueName());

		from("jms:" + mottakArkivPe.getQueueName() + ROUTE_OPTIONS)
				.log(INFO, log, "mottat melding fra mq mottakArkivPe")
				.to(PE_ARKIVER_BREV_ROUTE);
		from("jms:" + mottakOnlinePe.getQueueName() + ROUTE_OPTIONS)
				.log(INFO, log, "mottat melding fra mq mottakOnlinePe")
				.to(PE_ARKIVER_BREV_ROUTE);

		//Hent svar fra exstream
		from(PE_ARKIVER_BREV_ROUTE)
				.routeId(PE_ARKIVER_BREV_ROUTE)
				.routePolicy(arkiverBrevMetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.log(LoggingLevel.INFO, log, PE_ARKIVER_BREV_ROUTE + " starter behandlingen")
				.process(exchange -> {
					setDefaultReturnQueue(exchange, brevReplyPe.getQueueName());
				})
				.bean(peArkiverBrevService)
				.choice()
					.when(exchangeProperty(SENDTOMODE).isEqualTo(GI_TILBAKEMELDING))
						.log(INFO, log, "Sender tilbakemelding til: ${exchange.getIn().getHeader(\"" + OVERRIDE_DESTINATION + "\").toString()} eller default for pensjon")
						.to(JMS_OVERRIDDEN)
					.when(exchangeProperty(SENDTOMODE).isEqualTo(GI_FEILMELDING))
						.log(INFO, log, "Sender feilmelding til: ${exchange.getIn().getHeader(\"" + OVERRIDE_DESTINATION + "\").toString()}")
						.to(JMS_OVERRIDDEN)
					.otherwise()
						.to(JMS + deadletterPe.getQueueName())
						.log(ERROR, log, "En melding er sendt til deadletter pga ukjent mode!")
				.end();
		//@formatter:on
	}
}
