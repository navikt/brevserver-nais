package no.nav.brevserver.bestillBrev.pesys;

import config.ApplicationTestConfig;
import no.nav.brevserver.bestillBrev.Utils;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import java.util.concurrent.TimeUnit;

import static no.nav.brevserver.bestillBrev.Utils.BISYS_SYSTEM_ID;
import static no.nav.brevserver.bestillBrev.Utils.BREVREFERANSE;
import static no.nav.brevserver.bestillBrev.Utils.PENSJON_SYSTEM_ID;
import static no.nav.brevserver.bestillBrev.Utils.SYSTEM_PASSORD;
import static no.nav.brevserver.bestillBrev.Utils.createDefaultBrevstatus;
import static no.nav.brevserver.bestillBrev.Utils.createInput;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class})
@ActiveProfiles("itest")
@DirtiesContext
public class PeBestillBrevServiceTest {

	@Autowired
	private Queue onlinebrevPe;
	@Autowired
	private Queue dialogueOnlinePe;
	@Autowired
	private Queue deadletterPe;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Queue svarKo;
	@MockBean
	private BrevstatusService brevstatusServiceMock;
	@MockBean
	private BrevtilgangService brevtilgangServiceMock;

	private final String CORRELATION_ID = "abcd-1234-def-5678";
	private final String CALL_ID = "12-callID-34";
	private final String SVARKOSTRING = "queue:///SvarKo?targetClient=1";

	@Test
	public void shouldFailOnNullInput() {
		sendStringMessage(onlinebrevPe, null, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			ActiveMQMessage recieved = receive(deadletterPe);
			assertEquals(recieved.getJMSCorrelationID(), (CORRELATION_ID));
			verifyNoInteractions(brevtilgangServiceMock, brevstatusServiceMock);
		});
	}

	@Test
	public void shouldHandleMessage() throws Exception {
		when(brevtilgangServiceMock.sjekkSystemTilgang(PENSJON_SYSTEM_ID, SYSTEM_PASSORD)).thenReturn(true);
		when(brevstatusServiceMock.hentBrevStatus(PENSJON_SYSTEM_ID, BREVREFERANSE)).thenReturn(null);
		when(brevstatusServiceMock.lagreBrevStatus(any(BrevStatusVO.class))).thenReturn(createDefaultBrevstatus(PENSJON_SYSTEM_ID));

		String header = createInput(PENSJON_SYSTEM_ID);
		sendStringMessage(onlinebrevPe, header, CORRELATION_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(dialogueOnlinePe);
			assertEquals(recieved, Utils.getHappyPathText(PENSJON_SYSTEM_ID));
			verify(brevstatusServiceMock, times(1)).lagreBrevStatus(any(BrevStatusVO.class));
		});
	}

	@Test
	public void shouldFailOnBisys() throws Exception {
		when(brevtilgangServiceMock.sjekkSystemTilgang(BISYS_SYSTEM_ID, SYSTEM_PASSORD)).thenReturn(true);
		when(brevstatusServiceMock.hentBrevStatus(BISYS_SYSTEM_ID, BREVREFERANSE)).thenReturn(null);
		when(brevstatusServiceMock.lagreBrevStatus(any(BrevStatusVO.class))).thenReturn(createDefaultBrevstatus(PENSJON_SYSTEM_ID));

		String header = createInput(BISYS_SYSTEM_ID);
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletterPe);
			assertEquals(recieved, header);
			verifyNoInteractions(brevstatusServiceMock);
			verifyNoInteractions(brevtilgangServiceMock);
		});
	}

	@Test
	public void shouldFailOnBadXml() throws Exception {
		when(brevtilgangServiceMock.sjekkSystemTilgang(BISYS_SYSTEM_ID, SYSTEM_PASSORD)).thenReturn(true);
		when(brevstatusServiceMock.hentBrevStatus(BISYS_SYSTEM_ID, BREVREFERANSE)).thenReturn(null);
		when(brevstatusServiceMock.lagreBrevStatus(any(BrevStatusVO.class))).thenReturn(createDefaultBrevstatus(PENSJON_SYSTEM_ID));

		String header = "<rtv-brev>badXMl<rtv-brev>";
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletterPe);
			assertEquals(recieved, header);
			verifyNoInteractions(brevstatusServiceMock);
			verifyNoInteractions(brevtilgangServiceMock);
		});
	}

	@Test
	public void shouldFailOnEmptyFagsystem() throws Exception {
		when(brevtilgangServiceMock.sjekkSystemTilgang(BISYS_SYSTEM_ID, SYSTEM_PASSORD)).thenReturn(true);
		when(brevstatusServiceMock.hentBrevStatus(BISYS_SYSTEM_ID, BREVREFERANSE)).thenReturn(null);
		when(brevstatusServiceMock.lagreBrevStatus(any(BrevStatusVO.class))).thenReturn(createDefaultBrevstatus(PENSJON_SYSTEM_ID));

		String header = createInput("");
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletterPe);
			assertEquals(recieved, header);
			verifyNoInteractions(brevstatusServiceMock);
			verifyNoInteractions(brevtilgangServiceMock);
		});
	}

	@Test
	public void brevFinnesAllerede() throws Exception {
		when(brevtilgangServiceMock.sjekkSystemTilgang(PENSJON_SYSTEM_ID, SYSTEM_PASSORD)).thenReturn(true);
		when(brevstatusServiceMock.hentBrevStatus(PENSJON_SYSTEM_ID, BREVREFERANSE)).thenReturn(createDefaultBrevstatus(PENSJON_SYSTEM_ID));
		when(brevstatusServiceMock.lagreBrevStatus(any(BrevStatusVO.class))).thenReturn(createDefaultBrevstatus(PENSJON_SYSTEM_ID));

		String header = createInput(PENSJON_SYSTEM_ID);
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(SVARKOSTRING);
			assertEquals(recieved, Utils.getBrevFinnesAlleredeString(PENSJON_SYSTEM_ID));
			verify(brevstatusServiceMock, times(1)).hentBrevStatus(anyString(), anyString());
			verify(brevstatusServiceMock, times(0)).lagreBrevStatus(any(BrevStatusVO.class));

		});
	}

	private <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement) response).getValue();
		}
		return (T) response;
	}

	private <T> T receive(String queue) {
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
			msg.setJMSCorrelationID(CORRELATION_ID);
			msg.setJMSReplyTo(svarKo);
			if (callId != null) {
				msg.setStringProperty("callId", callId);
			}
			return msg;
		});
	}

}