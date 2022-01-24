import config.AbstractDatabaseTest;
import config.ApplicationTestConfig;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.service.BrevstatusService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import utils.Utils;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static utils.Utils.BISYS_SYSTEM_ID;
import static utils.Utils.BREVREFERANSE2;
import static utils.Utils.CALLID;
import static utils.Utils.STATUS_FERDIG;
import static utils.Utils.classpathToString;
import static utils.Utils.createBisysKvittering2;
import static utils.Utils.createBrevstatus;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class})
@ActiveProfiles("itest")

//TODO:  Fjern. Ser ikke mer på problemet nå da det kan hende modulen deles opp
@DirtiesContext
@Transactional
@Ignore
public class ArkiverBrevRouteIT  extends AbstractDatabaseTest {

	@Inject
	private Queue mottakArkiv;
	@Inject
	private Queue deadletter;
	@Inject
	private JmsTemplate jmsTemplate;
	@Inject
	private Queue svarKo;
	@Inject
	private BrevstatusService brevstatusService;

	@Test
	//happypath
	public void shouldHandleMessage() throws Exception{
		lagreDefaultBrevStatusVo();
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
		String header = Utils.createBisysKvittering();
		sendStringMessage(mottakArkiv, header + "Dette er en pdf".getBytes(), CALLID);
		await().atMost(100, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(svarKo);
			assertThat(recieved.equals(classpathToString("svarXml/happySvarko.xml")));
		});
		TestTransaction.flagForCommit();
		TestTransaction.end();

		BrevStatusVO endretBrevstatusVo  = brevstatusService.hentBrevStatus(Utils.BREVREFERANSE, BISYS_SYSTEM_ID);
		assertThat(endretBrevstatusVo.getStatus().equals(STATUS_FERDIG));
	}

	@Test
	public void shouldCreateNewBrevStatus() throws BrevTechnicalException {
		String header = createBisysKvittering2();
		sendStringMessage(mottakArkiv, header + "Dette er en pdf".getBytes(), CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(svarKo);
			assertThat(recieved.equals(classpathToString("svarXml/happySvarko.xml")));
		});
		TestTransaction.flagForCommit();
		TestTransaction.end();

		BrevStatusVO endretBrevstatusVo  = brevstatusService.hentBrevStatus(BREVREFERANSE2, BISYS_SYSTEM_ID);
		assertThat(endretBrevstatusVo.getStatus().equals(STATUS_FERDIG));
	}

	@Test
	public void shouldSendToFeilko() throws Exception{
		String header = Utils.createBadXmlKvitteringHeader();
		sendStringMessage(mottakArkiv, header, CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletter);
			assertThat(recieved.equals(classpathToString("svarXml/deadletterQ.xml")));
			assertNotNull(recieved);
			System.out.println(recieved);
		});
	}


	private void lagreDefaultBrevStatusVo() throws BrevTechnicalException {
		BrevStatusVO brevstatus = createBrevstatus(BISYS_SYSTEM_ID, Utils.BREVREFERANSE);
		brevstatusService.lagreBrevStatus(brevstatus);
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
			msg.setJMSCorrelationID("Dette-er-en-correlation-ID");
			msg.setJMSReplyTo(svarKo);
			if (callId != null) {
				msg.setStringProperty("callId", callId);
			}
			return msg;
		});
	}
}