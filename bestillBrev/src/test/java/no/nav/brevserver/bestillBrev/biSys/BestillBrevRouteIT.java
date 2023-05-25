package no.nav.brevserver.bestillBrev.biSys;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import config.AbstractTest;
import no.nav.brevserver.bestillBrev.Utils;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;

import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import java.util.concurrent.TimeUnit;

import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_BREVPAKKE;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@DirtiesContext
public class BestillBrevRouteIT extends AbstractTest {

	@Autowired
	private Queue onlinebrev;
	@Autowired
	private Queue deadletter;
	@Autowired
	private Queue dialogueOnline;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Queue svarKo;
	@Autowired
	private BrevstatusService brevstatusService;
	@Autowired
	private BrevtilgangService brevtilgangService;

	private final String BREVREF_XML = "3835845842";

	@Test
	public void shouldBestillNewBrev() throws Exception{
		String message = Utils.classpathToString("brevXml/bisysBrev.xml");
		sendStringMessage(onlinebrev, message, Utils.CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(dialogueOnline);
			assertNotNull(recieved);
		});
		TestTransaction.flagForCommit();
		TestTransaction.end();

		BrevStatusVO endretBrevstatusVo  = brevstatusService.hentBrevStatus(BREVREF_XML, Utils.BISYS_SYSTEM_ID);
		assertEquals(BREVSTATUS_BREVPAKKE, endretBrevstatusVo.getStatus());
	}

	@Test
	public void shouldGiTilgang() throws Exception{
		Logger loggen =(Logger) LoggerFactory.getLogger(BestillBrevRoute.class);
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		listAppender.start();
		loggen.addAppender(listAppender);
		String message = Utils.classpathToString("brevXml/fraBrevlager.xml");
		sendStringMessage(onlinebrev, message, Utils.CALLID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			listAppender.list.contains("TIlgang gitt. Håndtering avsluttes");
		});
		TestTransaction.flagForCommit();
		TestTransaction.end();
		listAppender.list.contains("TIlgang gitt. Håndtering avsluttes");

		assertTrue(brevtilgangService.sjekkTilgang("BI12", "92fa00f8d8024b0", "klientToken"));
	}

	@Test
	public void shouldSendToFeilKoOnException() throws Exception{

		String badHeader = Utils.classpathToString("brevXml/pensjonsbrev.xml");
		sendStringMessage(onlinebrev, badHeader, Utils.CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletter);
			assertEquals(recieved, Utils.classpathToString("brevXml/pensjonsbrev.xml"));
		});
		TestTransaction.flagForCommit();
		TestTransaction.end();
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