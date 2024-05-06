package no.nav.brevserver.arkiverBrev.Pesys;

import jakarta.jms.Queue;
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
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_TILBAKEMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.setDefaultReturnQueue;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.WARN;

@Component
public class PeArkiverBrevRoute extends RouteBuilder {
	public static final String PE_ARKIVER_BREV_ROUTE = "direct:peArkiverBrev";
	private static final String ROUTE_OPTIONS = "?transacted=true&concurrentConsumers=1&maxMessagesPerTask=100";


	private final Queue mottakArkivPeLinux;
	private final Queue mottakOnlinePeLinux;
	private final Queue deadletterPe;
	private final Queue brevReplyPe;
	private final PeArkiverBrevService peArkiverBrevService;

	public PeArkiverBrevRoute(Queue mottakArkivPeLinux,
							  Queue mottakOnlinePeLinux,
							  Queue deadletterPe,
							  Queue brevReplyPe,
							  PeArkiverBrevService peArkiverBrevService) {
		this.mottakArkivPeLinux = mottakArkivPeLinux;
		this.mottakOnlinePeLinux = mottakOnlinePeLinux;
		this.deadletterPe = deadletterPe;
		this.brevReplyPe = brevReplyPe;
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

		onException(ValidationException.class, BrevFunctionalException.class)
				.handled(true)
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.log(WARN, log, "${exception}; ")
				.to(JMS + deadletterPe.getQueueName());

		from("jms:" + mottakArkivPeLinux.getQueueName() + ROUTE_OPTIONS)
				.log(INFO, log, PE_ARKIVER_BREV_ROUTE + " starter behandlingen av melding fra: " + mottakArkivPeLinux.getQueueName())
				.to(PE_ARKIVER_BREV_ROUTE);
		from("jms:" + mottakOnlinePeLinux.getQueueName() + ROUTE_OPTIONS)
				.log(INFO, log, PE_ARKIVER_BREV_ROUTE + " starter behandlingen av melding fra: " + mottakOnlinePeLinux.getQueueName())
				.to(PE_ARKIVER_BREV_ROUTE);

		//Hent svar fra exstream
		from(PE_ARKIVER_BREV_ROUTE)
				.routeId(PE_ARKIVER_BREV_ROUTE)
				.setExchangePattern(ExchangePattern.InOnly)
				.process(new MdcSetterProcessor())
				.process(exchange -> setDefaultReturnQueue(exchange, brevReplyPe.getQueueName()))
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
				.end()
				.process(new MdcRemoverProcessor());
		//@formatter:on
	}
}
