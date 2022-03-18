package no.nav.brevserver.bestillBrev.biSys;

import com.ibm.msg.client.jms.DetailedJMSException;
import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.bestillBrev.BestillBrevMetricsRoutePolicy;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.Queue;

import static no.nav.brevserver.core.utils.ExchangeUtils.DEFAULT_RETURN_QUEUE;
import static no.nav.brevserver.core.utils.ExchangeUtils.DESTINATION;
import static no.nav.brevserver.core.utils.ExchangeUtils.JMS;
import static no.nav.brevserver.core.utils.ExchangeUtils.JMS_OVERRIDDEN;
import static no.nav.brevserver.core.utils.ExchangeUtils.OVERRIDE_DESTINATION;
import static no.nav.brevserver.core.utils.ExchangeUtils.SENDTOMODE;
import static no.nav.brevserver.core.utils.ExchangeUtils.SENDTOMODE;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_FEILMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.INGEN_TILBAKEMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.OPPRETT_BREV;
import static no.nav.brevserver.core.utils.ExchangeUtils.overrideDestination;
import static no.nav.brevserver.core.utils.ExchangeUtils.overrideDestinationWithTargetClient;
import static no.nav.brevserver.core.utils.ExchangeUtils.setDefaultReturnQueue;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;

@Component
@Slf4j
public class BestillBrevRoute extends RouteBuilder {
	public static final String BESTILL_BREV_ROUTE = "direct:bestillBrev";
	public static final String BESTILLBREV = "bestill_brev";
	private final String ROUTE_OPTIONS = "?transacted=true&concurrentConsumers=1";//&mapJmsMessage=false";

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

	/*
	config for new exstream:
	    "DIALOGUE_ONLINE_QUEUENAME": "QA.Q1_EDP.BISYS_ONLINE",
  		"MOTTAK_ARKIV_QUEUENAME": "QA.Q1_EDP.BS_BISYS_MOTTAK_ARKIV",
  		"MOTTAK_ONLINE_QUEUENAME": "QA.Q1_EDP.BS_BISYS_REDIGERBART_DOK",
	config for old:
		  "DIALOGUE_ONLINE_QUEUENAME": "QA.Q475.DIALOGUE_ONLINE",
		  "MOTTAK_ARKIV_QUEUENAME": "QA.Q475.BREVSERVER_MOTTAK_ARKIV",
		  "MOTTAK_ONLINE_QUEUENAME": "QA.Q475.BREVSERVER_MOTTAK_ONLINE",
	 */

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
				.log(LoggingLevel.WARN, "DetailedJMSException oppstått i BestillBrevRoute.")
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(true)
				.logStackTrace(true)
				.handled(true)
				.to("jms:" + deadletter.getQueueName());


		from("jms:" + onlinebrev.getQueueName() + ROUTE_OPTIONS)
				.log(INFO, log, "Starter behandlingen")
				.to(BESTILL_BREV_ROUTE);
		
		//Brevbestilling fra Bisys
		from(BESTILL_BREV_ROUTE)
				.routeId(BESTILLBREV)
				.routePolicy(bestillBrevMetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.log(INFO, log, BESTILLBREV + " starter behandlingen")
				.bean(bestillBrevService)
				.process(exchange -> {
					log.info("XML til Exstream: " + exchange.getIn().getBody());
					setDefaultReturnQueue(exchange, deadletter.getQueueName());
				})
				.choice()
					.when(exchangeProperty(SENDTOMODE).isEqualTo(OPPRETT_BREV))
						.process(exchange -> {
							overrideDestinationWithTargetClient(exchange, dialogueOnline.getQueueName());
						})
						.to(JMS_OVERRIDDEN)
						.log(INFO, log, "Brev sendt til opprettelse i Exstream: " + dialogueOnline.getQueueName())
					.when(exchangeProperty(SENDTOMODE).isEqualTo(GI_FEILMELDING ))
						.to(JMS_OVERRIDDEN)
						.log(INFO, log, "Feilmelding er sendt til: JMS_OVERRIDDEN")
					.when(exchangeProperty(SENDTOMODE).isEqualTo(INGEN_TILBAKEMELDING))
						.log(INFO, log, "TIlgang gitt. Håndtering avsluttes")
						.stop()
					.otherwise()
						.to(JMS + deadletter.getQueueName())
						.log(ERROR, log, "En melding er sendt til deadletter pga ukjent mode!")
				.end();

	}
}

