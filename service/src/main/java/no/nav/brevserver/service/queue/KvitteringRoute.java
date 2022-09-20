package no.nav.brevserver.service.queue;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static no.nav.brevserver.core.utils.ExchangeUtils.JMS_OVERRIDDEN;
import static no.nav.brevserver.core.utils.ExchangeUtils.OVERRIDE_DESTINATION;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;

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
				.to(JMS_OVERRIDDEN);
	}
}