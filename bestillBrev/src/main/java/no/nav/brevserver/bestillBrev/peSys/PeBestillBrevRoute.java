package no.nav.brevserver.bestillBrev.peSys;

import com.ibm.msg.client.jms.DetailedJMSException;
import no.nav.brevserver.bestillBrev.BestillBrevMetricsRoutePolicy;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.Queue;

import static no.nav.brevserver.core.utils.ExchangeUtils.JMS;
import static no.nav.brevserver.core.utils.ExchangeUtils.JMS_OVERRIDDEN;
import static no.nav.brevserver.core.utils.ExchangeUtils.OVERRIDE_DESTINATION;
import static no.nav.brevserver.core.utils.ExchangeUtils.SENDTOMODE;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_FEILMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.INGEN_TILBAKEMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.OPPRETT_BREV;
import static no.nav.brevserver.core.utils.ExchangeUtils.setDefaultReturnQueue;
import static no.nav.brevserver.core.utils.ExchangeUtils.setDestination;
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
	private final Queue brevReplyPe;
	private final BestillBrevMetricsRoutePolicy bestillBrevMetricsRoutePolicy;
	private final PeBestillBrevService peBestillBrevService;

	@Inject
	public PeBestillBrevRoute(Queue onlinebrevPe,
							  Queue deadletterPe,
							  Queue dialogueOnlinePe,
							  //TODO: PeBestillBrevMetrics? Unødvendig? Undersøk!
							  Queue brevReplyPe, BestillBrevMetricsRoutePolicy peBestillBrevMetricsRoutePolicy,
							  PeBestillBrevService peBestillBrevService) {
		this.onlinebrevPe = onlinebrevPe;
		this.deadletterPe = deadletterPe;
		this.dialogueOnlinePe = dialogueOnlinePe;
		this.brevReplyPe = brevReplyPe;
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
				.log(LoggingLevel.WARN, "DetailedJMSException oppstått i PeBestillBrevRoute")
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(true)
				.handled(true)
				.to("jms:" + deadletterPe.getQueueName());


		from("jms:" + onlinebrevPe.getQueueName() + ROUTE_OPTIONS)
				.log(INFO, log, "mottat melding fra mq")
				.to(BESTILL_BREV_ROUTE_PE);

		//Brevbestilling fra Pesys
		from(BESTILL_BREV_ROUTE_PE)
				.routeId(BESTILLBREV)
				.routePolicy(bestillBrevMetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.log(LoggingLevel.INFO, log, BESTILLBREV + " starter behandlingen")
				.process(exchange -> {
					log.info("XML til Exstream: " + exchange.getIn().getBody());
					setDefaultReturnQueue(exchange, brevReplyPe.getQueueName());
				})
				.bean(peBestillBrevService)
				.choice()
					.when(exchangeProperty(SENDTOMODE).isEqualTo(OPPRETT_BREV))
						.to(JMS + dialogueOnlinePe.getQueueName())
						.log(INFO, log, "Brev sendt til opprettelse i Exstream: " + dialogueOnlinePe.getQueueName())
					.when(exchangeProperty(SENDTOMODE).isEqualTo(GI_FEILMELDING ))
						.log(INFO, log, "Feilmelding er sendt til:: ${exchange.getIn().getHeader(\"" + OVERRIDE_DESTINATION + "\").toString()}")
						.to(JMS_OVERRIDDEN)
					.when(exchangeProperty(SENDTOMODE).isEqualTo(INGEN_TILBAKEMELDING))
						.log(INFO, log, "Tilgang gitt. Håndtering avsluttes")
						.stop()
					.otherwise()
						.to(JMS + deadletterPe.getQueueName())
						.log(ERROR, log, "En melding er sendt til deadletter pga ukjent mode!")
				.end();

	}
}
