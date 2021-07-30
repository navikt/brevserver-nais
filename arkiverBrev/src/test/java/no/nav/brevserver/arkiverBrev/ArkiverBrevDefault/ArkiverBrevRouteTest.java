package no.nav.brevserver.arkiverBrev.ArkiverBrevDefault;

import io.micrometer.core.instrument.util.IOUtils;
import no.nav.brevserver.arkiverBrev.config.ApplicationTestConfig;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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


	//Test for å gjøre det lettere å lage routen riktig
	@Test
	public void shouldHandleMessage() throws Exception{
		System.out.println();
		//String stringToSend = new String( .getBytes(StandardCharsets.ISO_8859_1));
		//String stringsendTo = new String(stringToSend);
		sendStringMessage(mottakArkiv, classpathToString("inMessage.xml"), "callId");
		await().atMost(120, TimeUnit.SECONDS).untilAsserted(() -> {
			//Satt routen til å spytte ut meldingen til deadletter for at den skal kunne kjøre opp.
			//OBS: Ved exceptions kommer også meldingen hit. Debug for å se hva som skjer
			String recieved = receive(deadletter);
			assertNotNull(recieved);
		});
	}

	private <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement) response).getValue();
		}
		return (T) response;
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