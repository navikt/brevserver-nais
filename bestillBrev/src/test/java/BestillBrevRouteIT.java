
import config.AbstractDatabaseTest;
import config.ApplicationTestConfig;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class})
@ActiveProfiles("itest")

//TODO:  Fjern. Ser ikke mer på problemet nå da det kan hende modulen deles opp
@DirtiesContext
@Transactional
public class BestillBrevRouteIT extends AbstractDatabaseTest {

	@Inject
	private Queue onlinebrev;
	@Inject
	private Queue deadletter;
	@Inject
	private JmsTemplate jmsTemplate;
	@Inject
	private Queue svarKo;
	@Inject
	private BrevstatusService brevstatusService;
	@Inject
	private BrevtilgangService brevtilgangService;

	private final String BREVREF_XML = "3835845842";


	@Test
	public void shouldHandleMessage() throws Exception{

		String message = Utils.classpathToString("/bestillBrev/bisysBrev.xml");
		sendStringMessage(onlinebrev, message, Utils.CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(svarKo);
			assertNotNull(recieved);
			System.out.println(recieved);
		});
		TestTransaction.flagForCommit();
		TestTransaction.end();

		BrevStatusVO endretBrevstatusVo  = brevstatusService.hentBrevStatus(BREVREF_XML, Utils.BISYS_SYSTEM_ID);
		assertThat(Utils.STATUS_KLADD.equals(endretBrevstatusVo.getStatus()));
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
}