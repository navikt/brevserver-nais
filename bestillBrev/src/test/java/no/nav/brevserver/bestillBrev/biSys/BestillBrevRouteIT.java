package no.nav.brevserver.bestillBrev.biSys;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import config.AbstractDatabaseTest;
import config.ApplicationTestConfig;
import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.bestillBrev.Utils;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.SysTilgangVO;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import java.util.concurrent.TimeUnit;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_BREVPAKKE;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class})
@ActiveProfiles("itest")
@Transactional
@Slf4j
public class BestillBrevRouteIT extends AbstractDatabaseTest {

	@Inject
	private Queue onlinebrev;
	@Inject
	private Queue deadletter;
	@Inject
	private Queue dialogueOnline;
	@Inject
	private JmsTemplate jmsTemplate;
	@Inject
	private Queue svarKo;
	@Inject
	private BrevstatusService brevstatusService;
	@Inject
	private BrevtilgangService brevtilgangService;

	private final String BREVREF_XML = "3835845842";
	private final String BISYS = "BI12";


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