package no.nav.brevserver.service.queue;

import com.ibm.msg.client.jms.DetailedJMSException;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.apache.camel.LoggingLevel.ERROR;

@Component
public class KvitteringRoute extends RouteBuilder {

	public static final String DIRECT_SENDKVITTERINGROUTE = "direct:sendkvitteringroute";

	@Override
	public void configure() throws Exception {
		errorHandler(defaultErrorHandler()
				.maximumRedeliveries(0)
				.log(log)
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(true)
				.loggingLevel(ERROR));

		from(DIRECT_SENDKVITTERINGROUTE)
				.log(LoggingLevel.INFO, log, "Starter behandlingen av kvitteringsmelding")
				.toD("jms:${header.uri}")
				.log(LoggingLevel.INFO, log, "Kvitteringsmeldingen er sendt til: " + "${header.uri}");
	}
}