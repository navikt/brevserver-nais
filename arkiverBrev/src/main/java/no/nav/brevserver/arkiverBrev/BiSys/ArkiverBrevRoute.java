package no.nav.brevserver.arkiverBrev.BiSys;

import com.ibm.msg.client.jms.DetailedJMSException;
import no.nav.brevserver.arkiverBrev.ArkiverBrevMetricsRoutePolicy;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.Queue;

import static no.nav.brevserver.core.utils.ExchangeUtils.JMS;
import static no.nav.brevserver.core.utils.ExchangeUtils.JMSReplyTo;
import static no.nav.brevserver.core.utils.ExchangeUtils.JMS_OVERRIDDEN;
import static no.nav.brevserver.core.utils.ExchangeUtils.OVERRIDE_DESTINATION;
import static no.nav.brevserver.core.utils.ExchangeUtils.SENDTOMODE;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_FEILMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_TILBAKEMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.setDefaultReturnQueue;
import static org.apache.camel.ExchangePattern.InOnly;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.WARN;

@Component
public class ArkiverBrevRoute extends RouteBuilder {
	public static final String ARKIVER_BREV_ROUTE = "direct:arkiverBrev";
	private final String ROUTE_OPTIONS = "?transacted=true&concurrentConsumers=1";//&mapJmsMessage=false";


	private final Queue mottakArkiv;
	private final Queue mottakOnline;
	private final Queue deadletter;
	private final ArkiverBrevMetricsRoutePolicy arkiverBrevMetricsRoutePolicy;
	private final ArkiverBrevService arkiverBrevService;


	@Inject
	public ArkiverBrevRoute(Queue mottakArkiv,
							Queue mottakOnline,
							Queue deadletter,
							ArkiverBrevMetricsRoutePolicy arkiverBrevMetricsRoutePolicy,
							ArkiverBrevService arkiverBrevService) {
		this.mottakArkiv = mottakArkiv;
		this.mottakOnline = mottakOnline;
		this.deadletter = deadletter;
		this.arkiverBrevMetricsRoutePolicy = arkiverBrevMetricsRoutePolicy;
		this.arkiverBrevService = arkiverBrevService;
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

		onException(ValidationException.class)
				.handled(true)
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.log(WARN, log, "${exception}; ")
				.to(InOnly, "jms:" + deadletter.getQueueName());

		onException(BrevTechnicalException.class)
				.handled(true)
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.log(ERROR, log, "${exception}; ")
				.to(InOnly, "jms:" + deadletter.getQueueName());


		onException(DetailedJMSException.class)
				.log(WARN, "DetailedJMSException oppstått i ArkiverBrevRoute. ${exception};" )
				.useOriginalMessage()
				.logExhaustedMessageBody(true)
				.logExhaustedMessageHistory(true)
				.logStackTrace(true)
				.handled(true)
				.to(InOnly, "jms:" + deadletter.getQueueName());

		from("jms:" + mottakArkiv.getQueueName() + ROUTE_OPTIONS)
				.to(ARKIVER_BREV_ROUTE);
		from("jms:" + mottakOnline.getQueueName() + ROUTE_OPTIONS)
				.to(ARKIVER_BREV_ROUTE);

		//Hent svar fra exstream
		from(ARKIVER_BREV_ROUTE)
				.routeId(ARKIVER_BREV_ROUTE)
				.routePolicy(arkiverBrevMetricsRoutePolicy)
				.setExchangePattern(InOnly)
				.log(LoggingLevel.INFO, log, ARKIVER_BREV_ROUTE + " starter arkiveringen av ny melding fra Bisys")
				.process(exchange -> {
					setDefaultReturnQueue(exchange, deadletter.getQueueName());
				})
				.bean(arkiverBrevService)
				.removeHeader(JMSReplyTo)
				.choice()
					.when(exchangeProperty(SENDTOMODE).isEqualTo(GI_TILBAKEMELDING))
						.log(INFO, log, "Sender svar til destinasjon " + OVERRIDE_DESTINATION + "=${header." + OVERRIDE_DESTINATION + "}")
						.to(InOnly, JMS_OVERRIDDEN)
				.when(exchangeProperty(SENDTOMODE).isEqualTo(GI_FEILMELDING))
						.log(INFO, log, "Sender feilmelding til destinasjon " + OVERRIDE_DESTINATION + "=${header." + OVERRIDE_DESTINATION + "}")
						.to(InOnly, JMS_OVERRIDDEN)
					.otherwise()
						.to(InOnly, JMS + deadletter.getQueueName())
						.log(ERROR, log, "En melding er sendt til destinasjon deadletter pga ukjent mode!")
				.end();
		//@formatter:on
	}
}
