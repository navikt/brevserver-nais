package no.nav.brevserver.arkiverBrev.ArkiverBrevDefault;

import io.micrometer.core.instrument.util.IOUtils;
import no.nav.brevserver.arkiverBrev.config.ApplicationTestConfig;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class})
@ActiveProfiles("itest")
public class ArkiverBrevRouteTest {

	@Inject
	private Queue mottakArkiv;
	@Inject
	private Queue mottakOnline;
	@Inject
	private Queue deadletter;
	@Inject
	private JmsTemplate jmsTemplate;

	@Value("${mottak_arkiv.queuename}")
	String mottakArkivQueueName;


	@Test
	public void shouldHandleMessage() throws Exception{
		System.out.println();
		sendStringMessage(mottakArkiv, "text", "callId");
	}

	private void sendStringMessage(Queue queue, final String message, final String callId) {
		jmsTemplate.send(queue, session -> {
			TextMessage msg = new ActiveMQTextMessage();
			msg.setText(message);
			if (callId != null) {
				msg.setStringProperty("callId", callId);
			}
			return msg;
		});
	}

	public static String classpathToString(String classpathResource) throws IOException {
		InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
		return IOUtils.toString(inputStream, UTF_8);
	}
}