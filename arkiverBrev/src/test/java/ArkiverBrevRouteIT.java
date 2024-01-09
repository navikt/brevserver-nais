import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.xml.bind.JAXBElement;
import lombok.SneakyThrows;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.service.BrevstatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.transaction.TestTransaction;
import utils.Utils;

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


public class ArkiverBrevRouteIT  extends AbstractTest {
	@Autowired
	private Queue mottakArkiv;
	@Autowired
	private Queue deadletter;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Queue svarKo;
	@Autowired
	private BrevstatusService brevstatusService;

	private static final String CORRELATION_ID="1890432+12342341";
	//Kan ikke bruke selve køen da vi legger på ?targetclient=1 på kønavnet i servicen.
	private static final String SVARKOSTRING = "queue:///mottakSvarKo?targetClient=1";

	@Test
	//happypath
	public void shouldHandleMessage() throws Exception{
		lagreDefaultBrevStatusVo();
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		assertThat(brevstatusService.hentBrevStatus(Utils.BREVREFERANSE, BISYS_SYSTEM_ID).getStatus() != STATUS_FERDIG);

		String header = Utils.createBisysKvittering();
		sendStringMessage(mottakArkiv, header + "Dette er en pdf".getBytes(), CALLID);
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			Message recieved = jmsTemplate.receive(SVARKOSTRING);
			assertThat(recieved.getJMSCorrelationID().equals(CORRELATION_ID));
			assertThat(brevstatusService.hentBrevStatus(Utils.BREVREFERANSE, BISYS_SYSTEM_ID).getStatus().equals(STATUS_FERDIG));
		});
		TestTransaction.flagForCommit();
		TestTransaction.end();

	}

	@Test
	public void shouldCreateNewBrevStatus() throws BrevTechnicalException {
		String header = createBisysKvittering2();
		sendStringMessage(mottakArkiv, header + "Dette er en pdf".getBytes(), CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved2 = receive(SVARKOSTRING);
			assertThat(recieved2.equals(classpathToString("svarXml/happySvarko.xml")));
		});
		TestTransaction.flagForCommit();
		TestTransaction.end();

		BrevStatusVO endretBrevstatusVo  = brevstatusService.hentBrevStatus(BREVREFERANSE2, BISYS_SYSTEM_ID);
		assertThat(endretBrevstatusVo.getStatus().equals(STATUS_FERDIG));
	}

	@Test
	public void shouldSendToFeilko() {
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


	private <T> T receive(String queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement) response).getValue();
		}
		return (T) response;
	}

	private <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement) response).getValue();
		}
		return (T) response;
	}

	@SneakyThrows
	private void sendStringMessage(Queue queue, final String message, final String callId) {
		jmsTemplate.send(queue, session -> {
			TextMessage msg = session.createTextMessage();
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