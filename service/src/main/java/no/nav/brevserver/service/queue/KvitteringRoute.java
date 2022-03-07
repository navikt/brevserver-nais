package no.nav.brevserver.service.queue;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static no.nav.brevserver.core.utils.ExchangeUtils.JMS_OVERRIDDEN;
import static org.apache.camel.LoggingLevel.ERROR;

@Component
public class KvitteringRoute extends RouteBuilder {

	public static final String DIRECT_SENDKVITTERINGROUTE = "direct:sendkvitteringroute";

	@Override
	public void configure() {
		errorHandler(defaultErrorHandler()
				.maximumRedeliveries(0)
				.log(log)
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(true)
				.loggingLevel(ERROR));

		from(DIRECT_SENDKVITTERINGROUTE)
				.log(LoggingLevel.INFO, log, "Starter behandlingen av kvitteringsmelding")
				.to(JMS_OVERRIDDEN)
				.log(LoggingLevel.INFO, log, "Brevserver har levert kvitteringen");
	}
}