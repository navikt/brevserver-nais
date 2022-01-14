package no.nav.brevserver.arkiverBrev.BiSys;

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

import static no.nav.brevserver.core.utils.ExchangeUtils.DESTINATION;
import static no.nav.brevserver.core.utils.ExchangeUtils.JMS;
import static no.nav.brevserver.core.utils.ExchangeUtils.SENDTOMODE;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_FEILMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_TILBAKEMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.INGEN_TILBAKEMELDING;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;

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
				.log(LoggingLevel.WARN, "DetailedJMSException oppstått i arkiverBrev for forsendelse med  getIdsForLogging() . Melding sendt til funksjonell feilkø.")
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(false)
				.handled(true)
				.to("jms:" + deadletter.getQueueName());



		from("jms:" + mottakArkiv.getQueueName() + ROUTE_OPTIONS)
				.log(INFO, log, "mottat melding fra mq")
				.to(ARKIVER_BREV_ROUTE);
		from("jms:" + mottakOnline.getQueueName() + ROUTE_OPTIONS)
				.log(INFO, log, "mottat melding fra mq")
				.to(ARKIVER_BREV_ROUTE);

		//Hent svar fra exstream
		from(ARKIVER_BREV_ROUTE)
				.routeId(ARKIVER_BREV_ROUTE)
				.routePolicy(arkiverBrevMetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.log(LoggingLevel.INFO, log, ARKIVER_BREV_ROUTE + " starter behandlingen")
				.bean(arkiverBrevService)
				.process(exchange -> {
					if (exchange.getIn().getHeader(DESTINATION) == null)
						exchange.getIn().setHeader(DESTINATION, BISYS_DEFAULT! må grave den opp først..);
				})
				.choice()
					.when(exchangeProperty(SENDTOMODE).isEqualTo(GI_TILBAKEMELDING))
						.toD(JMS + header(DESTINATION))
						.log(INFO, log, "Tilbakemelding er sendt til: " + header(DESTINATION))
					.when(exchangeProperty(SENDTOMODE).isEqualTo(GI_FEILMELDING))
						.toD(JMS + header(DESTINATION))
						.log(INFO, log, "Feilmelding er sendt til: " + header(DESTINATION))
					.otherwise()
						.to(JMS + deadletter.getQueueName())
						.log(ERROR, log, "En melding er sendt til deadletter pga ukjent mode!")
				.end();

	}
}
