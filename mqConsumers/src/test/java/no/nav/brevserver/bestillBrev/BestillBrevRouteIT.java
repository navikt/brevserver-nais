package no.nav.brevserver.bestillBrev;

import io.micrometer.core.instrument.util.IOUtils;
import no.nav.brevserver.config.ApplicationTestConfig;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class})
@ActiveProfiles("itest")
public class BestillBrevRouteIT {

	@Inject
	private Queue onlinebrev;
	@Inject
	private Queue deadletter;
	@Inject
	private JmsTemplate jmsTemplate;
	@Inject
	private Queue svarKo;
	@MockBean
	private BrevstatusService brevstatusServiceMock;
	@MockBean
	private BrevlagerService brevlagerServiceMock;
	@MockBean
	private BrevtilgangService brevtilgangServiceMock;

	private final String brevreferanse = "3835845842";
	private final String systemId = "BI12";
	private final String passord = "*****";

	//Test for å gjøre det lettere å lage routen riktig
	@Test
	public void shouldHandleMessage() throws Exception{

		//when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenReturn(createDefaultBrevstatus());
		when(brevtilgangServiceMock.sjekkSystemTilgang(systemId, passord)).thenReturn(true);
		when(brevstatusServiceMock.hentBrevStatus(systemId, brevreferanse)).thenReturn(null);

		String message = classpathToString("/bestillBrev/bisysBrev.xml");
		sendStringMessage(onlinebrev, message, "Dette-er-en-callId");
		await().atMost(120, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(svarKo);
			assertNotNull(recieved);
			System.out.println(recieved);
		});
	}

	private BrevStatusVO createDefaultBrevstatus() {
		BrevStatusVO brevstatus = new BrevStatusVO();
		brevstatus.setSystemID(systemId);
		brevstatus.setBrevreferanse(brevreferanse);
		brevstatus.setPassord(passord);
		return brevstatus;
	}

	private <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		System.out.println("Recieved!");
		if (response instanceof JAXBElement) {
			response = ((JAXBElement) response).getValue();
		}
		return (T) response;
	}

	private void sendStringMessage(Queue queue, final String message, final String callId) {
		jmsTemplate.send(queue, session -> {
			TextMessage msg = new ActiveMQTextMessage();
			msg.setText(message);
			msg.setJMSCorrelationID("Dette-er-en-correlation-ID");
			msg.setJMSReplyTo(svarKo);
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