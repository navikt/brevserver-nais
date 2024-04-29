package no.nav.brevserver.arkiverBrev.BiSys;

import com.ibm.msg.client.jakarta.jms.DetailedJMSException;
import jakarta.jms.Queue;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.utils.MDC.MdcRemoverProcessor;
import no.nav.brevserver.core.utils.MDC.MdcSetterProcessor;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static no.nav.brevserver.core.utils.ExchangeUtils.JMS;
import static no.nav.brevserver.core.utils.ExchangeUtils.JMSReplyTo;
import static no.nav.brevserver.core.utils.ExchangeUtils.JMS_OVERRIDDEN;
import static no.nav.brevserver.core.utils.ExchangeUtils.OVERRIDE_DESTINATION;
import static no.nav.brevserver.core.utils.ExchangeUtils.SENDTOMODE;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_FEILMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.SendToMode.GI_TILBAKEMELDING;
import static no.nav.brevserver.core.utils.ExchangeUtils.setDefaultReturnQueue;
import static org.apache.camel.ExchangePattern.InOnly;
import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.WARN;

@Component
public class ArkiverBrevRoute extends RouteBuilder {
	public static final String ARKIVER_BREV_ROUTE = "direct:arkiverBrev";
	private final String ROUTE_OPTIONS = "?transacted=true&concurrentConsumers=1";

	private final Queue mottakArkiv;
	private final Queue mottakOnline;
	private final Queue mottakOnlineLinux;
	private final Queue deadletter;
	private final Queue mottakArkivBq;
	private final ArkiverBrevService arkiverBrevService;

	public ArkiverBrevRoute(Queue mottakArkiv,
							Queue mottakOnline,
							Queue mottakOnlineLinux,
							Queue deadletter,
							Queue mottakArkivBq,
							ArkiverBrevService arkiverBrevService) {
		this.mottakArkiv = mottakArkiv;
		this.mottakOnline = mottakOnline;
		this.mottakOnlineLinux = mottakOnlineLinux;
		this.deadletter = deadletter;
		this.mottakArkivBq = mottakArkivBq;
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

		onException(BrevFunctionalException.class, ValidationException.class)
				.handled(true)
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.log(WARN, log, "${exception}; ")
				.to(InOnly, "jms:" + deadletter.getQueueName());

		onException(BrevTechnicalException.class, DetailedJMSException.class)
				.useOriginalMessage()
				.logExhaustedMessageBody(true)
				.logExhaustedMessageHistory(true)
				.logStackTrace(true)
				.handled(true)
				.log(WARN, log, "${exception}; ")
				.to(InOnly, "jms:" + mottakArkivBq.getQueueName());

		from("jms:" + mottakArkiv.getQueueName() + ROUTE_OPTIONS)
				.to(ARKIVER_BREV_ROUTE);
		from("jms:" + mottakOnline.getQueueName() + ROUTE_OPTIONS)
				.to(ARKIVER_BREV_ROUTE);
		from("jms:" + mottakOnlineLinux.getQueueName() + ROUTE_OPTIONS)
				.to(ARKIVER_BREV_ROUTE);

		//Hent svar fra exstream
		from(ARKIVER_BREV_ROUTE)
				.log(INFO, log, ARKIVER_BREV_ROUTE + " starter behandlingen av melding fra: " + mottakArkiv.getQueueName() )
				.routeId(ARKIVER_BREV_ROUTE)
				.setExchangePattern(InOnly)
				.process(new MdcSetterProcessor())
				.process(exchange -> {
					setDefaultReturnQueue(exchange, deadletter.getQueueName());
				})
				.bean(arkiverBrevService)
				.removeHeader(JMSReplyTo)
				.choice()
					.when(exchangeProperty(SENDTOMODE).isEqualTo(GI_TILBAKEMELDING))
						//Tilbakemeldingen blir sendt i kvitteringService
						.log(DEBUG, log, "Kvitteringsmelding har blitt sendt via kvitteringService. Avslutter behandling")
						.stop()
					.when(exchangeProperty(SENDTOMODE).isEqualTo(GI_FEILMELDING))
						.log(INFO, log, "Sender feilmelding til destinasjon " + OVERRIDE_DESTINATION + "=${header." + OVERRIDE_DESTINATION + "}")
						.to(InOnly, JMS_OVERRIDDEN)
					.otherwise()
						.to(InOnly, JMS + deadletter.getQueueName())
						.log(ERROR, log, "En melding er sendt til destinasjon deadletter pga ukjent mode!")
				.end()
				.process(new MdcRemoverProcessor());
		//@formatter:on
	}
}
