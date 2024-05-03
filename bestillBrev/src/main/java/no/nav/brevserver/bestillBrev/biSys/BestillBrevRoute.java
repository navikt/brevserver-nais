package no.nav.brevserver.bestillBrev.biSys;

import jakarta.jms.Queue;
import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.utils.MDC.MdcRemoverProcessor;
import no.nav.brevserver.core.utils.MDC.MdcSetterProcessor;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

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
@Slf4j
public class BestillBrevRoute extends RouteBuilder {

	public static final String BESTILLBREV = "bestill_brev";
	private static final String ROUTE_OPTIONS = "?transacted=true&concurrentConsumers=1";

	private final Queue onlinebrev;
	private final Queue dialogueOnline;
	private final Queue deadletter;
	private final BestillBrevService bestillBrevService;

	public BestillBrevRoute(Queue onlinebrev, // input-kø for brevserver
							Queue deadletter,
							Queue dialogueOnline, // input-kø for exstrem hvor brevserver sender bestillingen
							BestillBrevService arkiverBrevService) {
		this.onlinebrev = onlinebrev;
		this.deadletter = deadletter;
		this.dialogueOnline = dialogueOnline;
		this.bestillBrevService = arkiverBrevService;
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

		onException(ValidationException.class, BrevFunctionalException.class)
				.handled(true)
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(true)
				.logStackTrace(true)
				.handled(true)
				.log(ERROR, log, "${exception}; ")
				.to("jms:" + deadletter.getQueueName());

		//Brevbestilling fra Bisys
		from("jms:" + onlinebrev.getQueueName() + ROUTE_OPTIONS)
				.routeId(BESTILLBREV)
				.setExchangePattern(ExchangePattern.InOnly)
				.process(new MdcSetterProcessor())
				.log(INFO, log, BESTILLBREV + " starter behandlingen av ny brevbestilling fra Bisys")
				.bean(bestillBrevService)
				.process(exchange -> setDefaultReturnQueue(exchange, deadletter.getQueueName()))
				.choice()
					.when(exchangeProperty(SENDTOMODE).isEqualTo(OPPRETT_BREV))
						.process(exchange -> setDestination(exchange, dialogueOnline.getQueueName()))
						.to(JMS_OVERRIDDEN)
						.log(INFO, log, "Brev sendt til opprettelse i Exstream: " + dialogueOnline.getQueueName())
					.when(exchangeProperty(SENDTOMODE).isEqualTo(GI_FEILMELDING ))
						.log(INFO, log, "Feilmelding er sendt til:: ${exchange.getIn().getHeader(\"" + OVERRIDE_DESTINATION + "\").toString()}")
						.to(JMS_OVERRIDDEN)
					.when(exchangeProperty(SENDTOMODE).isEqualTo(INGEN_TILBAKEMELDING))
						.log(INFO, log, "Tilgang gitt. Håndtering avsluttes")
						.stop()
					.otherwise()
						.to(JMS + deadletter.getQueueName())
						.log(ERROR, log, "En melding er sendt til deadletter pga ukjent mode!")
				.end()
				.process(new MdcRemoverProcessor());
		//@formatter:on
	}
}

