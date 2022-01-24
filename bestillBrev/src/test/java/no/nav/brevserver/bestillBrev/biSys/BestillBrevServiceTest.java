package no.nav.brevserver.bestillBrev.biSys;

import config.ApplicationTestConfig;
import no.nav.brevserver.bestillBrev.Utils;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import java.util.concurrent.TimeUnit;

import static no.nav.brevserver.bestillBrev.Utils.BISYS_SYSTEM_ID;
import static no.nav.brevserver.bestillBrev.Utils.BREVREFERANSE;
import static no.nav.brevserver.bestillBrev.Utils.SYSTEM_PASSORD;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class})
@ActiveProfiles("itest")
@DirtiesContext
@Ignore
public class BestillBrevServiceTest {

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
	private BrevtilgangService brevtilgangServiceMock;

	private final String CORRELATION_ID = "abcd-1234-def-5678";
	private final String CALL_ID="12-callID-34";
	@Test
	public void shouldFailOnNullInput(){
		sendStringMessage(onlinebrev, null, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			ActiveMQMessage recieved = receive(deadletter);
			assertEquals(recieved.getJMSCorrelationID(), (CORRELATION_ID));
			verifyZeroInteractions(brevtilgangServiceMock, brevstatusServiceMock);
		});
	}

	@Test
	public void shouldHandleMessage() throws Exception {
		PowerMockito.when(brevtilgangServiceMock.sjekkSystemTilgang(BISYS_SYSTEM_ID, SYSTEM_PASSORD)).thenReturn(true);
		PowerMockito.when(brevstatusServiceMock.hentBrevStatus(BISYS_SYSTEM_ID, BREVREFERANSE)).thenReturn(null);
		PowerMockito.when(brevstatusServiceMock.lagreBrevStatus(any(BrevStatusVO.class))).thenReturn(createDefaultBrevstatus());

		String header = createDefaultInput();
		sendStringMessage(onlinebrev, header, CORRELATION_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(svarKo);
			assertEquals(recieved, Utils.getHappyPathText());
			verify(brevstatusServiceMock, times(1)).lagreBrevStatus(any(BrevStatusVO.class));
		});
	}
	@Test
	public void shouldFailOnPe() throws Exception {
		PowerMockito.when(brevtilgangServiceMock.sjekkSystemTilgang(BISYS_SYSTEM_ID, SYSTEM_PASSORD)).thenReturn(true);
		PowerMockito.when(brevstatusServiceMock.hentBrevStatus(BISYS_SYSTEM_ID, BREVREFERANSE)).thenReturn(null);
		PowerMockito.when(brevstatusServiceMock.lagreBrevStatus(any(BrevStatusVO.class))).thenReturn(createDefaultBrevstatus());

		String header = createInput("PE01");
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletter);
			assertEquals(recieved, header);
			verifyZeroInteractions(brevstatusServiceMock);
			verifyZeroInteractions(brevtilgangServiceMock);
		});
	}

	@Test
	public void shouldFailOnBadXml() throws Exception {
		PowerMockito.when(brevtilgangServiceMock.sjekkSystemTilgang(BISYS_SYSTEM_ID, SYSTEM_PASSORD)).thenReturn(true);
		PowerMockito.when(brevstatusServiceMock.hentBrevStatus(BISYS_SYSTEM_ID, BREVREFERANSE)).thenReturn(null);
		PowerMockito.when(brevstatusServiceMock.lagreBrevStatus(any(BrevStatusVO.class))).thenReturn(createDefaultBrevstatus());

		String header = "<rtv-brev>badXMl<rtv-brev>";
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletter);
			assertEquals(recieved, header);
			verifyZeroInteractions(brevstatusServiceMock);
			verifyZeroInteractions(brevtilgangServiceMock);
		});
	}

	@Test
	public void shouldFailOnEmptyFagsystem() throws Exception {
		PowerMockito.when(brevtilgangServiceMock.sjekkSystemTilgang(BISYS_SYSTEM_ID, SYSTEM_PASSORD)).thenReturn(true);
		PowerMockito.when(brevstatusServiceMock.hentBrevStatus(BISYS_SYSTEM_ID, BREVREFERANSE)).thenReturn(null);
		PowerMockito.when(brevstatusServiceMock.lagreBrevStatus(any(BrevStatusVO.class))).thenReturn(createDefaultBrevstatus());

		String header = createInput("");
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletter);
			assertEquals(recieved, header);
			verifyZeroInteractions(brevstatusServiceMock);
			verifyZeroInteractions(brevtilgangServiceMock);
		});
	}

	@Test
	public void brevFinnesAllerede() throws Exception {
		PowerMockito.when(brevtilgangServiceMock.sjekkSystemTilgang(BISYS_SYSTEM_ID, SYSTEM_PASSORD)).thenReturn(true);
		PowerMockito.when(brevstatusServiceMock.hentBrevStatus(BISYS_SYSTEM_ID, BREVREFERANSE)).thenReturn(createDefaultBrevstatus());
		PowerMockito.when(brevstatusServiceMock.lagreBrevStatus(any(BrevStatusVO.class))).thenReturn(createDefaultBrevstatus());

		String header = createDefaultInput();
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(svarKo);
			assertEquals(recieved, Utils.getBrevFinnesAlleredeString());
			verify(brevstatusServiceMock, times(1)).hentBrevStatus(anyString(), anyString());
			verify(brevstatusServiceMock, times(0)).lagreBrevStatus(any(BrevStatusVO.class));

		});
	}

	@Test
	public void shouldSaveTilgangWhenFromBrevlager() throws Exception {
		PowerMockito.when(brevtilgangServiceMock.sjekkSystemTilgang(BISYS_SYSTEM_ID, SYSTEM_PASSORD)).thenReturn(true);
		PowerMockito.when(brevtilgangServiceMock.lagreTilgang(BISYS_SYSTEM_ID, BREVREFERANSE, "token")).thenReturn(true);

		String header = createInput(BISYS_SYSTEM_ID, "frabrevlager");
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			verify(brevtilgangServiceMock, times(1)).sjekkSystemTilgang(BISYS_SYSTEM_ID, SYSTEM_PASSORD);
			verify(brevtilgangServiceMock, times(1)).lagreTilgang(BISYS_SYSTEM_ID, BREVREFERANSE, "token");
			verifyZeroInteractions(brevstatusServiceMock);
		});
	}
	@Test
	public void shouldFailWhenBadPassword() throws Exception {
		PowerMockito.when(brevtilgangServiceMock.sjekkSystemTilgang(BISYS_SYSTEM_ID, SYSTEM_PASSORD)).thenReturn(false);

		String header = createDefaultInput();
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(svarKo);
			assertEquals(recieved, Utils.getBadPasswordString());
			verify(brevtilgangServiceMock, times(1)).sjekkSystemTilgang(BISYS_SYSTEM_ID, SYSTEM_PASSORD);
			verifyZeroInteractions(brevstatusServiceMock);
		});

	}




	private String createDefaultInput(){
		return createInput(BISYS_SYSTEM_ID);
	}

	private String createInput(String fagsystem){
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		builder.append("<rtv-brev direkteutskrift=\"NEI\" format=\"ENSIDIG\" malpakke=\"BI01.BI01X01\" sysid=\"").append(fagsystem).append("\" passord=\"Bisys123\" saksbehandler=\"B100946\">");
		addText(builder);
		return builder.toString();
	}

	private String createInput(String fagsystem, String modus){
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		builder.append("<rtv-brev direkteutskrift=\"NEI\" klientToken=\"token\" modus=\"").append(modus).append("\" malpakke=\"BI01.BI01X01\" sysid=\"").append(fagsystem).append("\" passord=\"Bisys123\" saksbehandler=\"B100946\">");
		addText(builder);
		return builder.toString();
	}

	private StringBuilder addText(StringBuilder builder){
		builder.append("<brev brevref=\"").append(BREVREFERANSE).append("\" spraak=\"NB\" tknr=\"0814\">");
		builder.append("<brevMottaker>");
		builder.append("<navn>").append("Donald").append("</navn>");
		builder.append("<adr1>").append("Andeby 1").append("</adr1>");
		builder.append("<adr2>").append("Borte").append("</adr2>");
		builder.append("<adr3>").append("vekk").append("</adr3>");
		builder.append("<adr4/>");
		builder.append("<bidrRolle>").append("01").append("</bidrRolle>");
		builder.append("<fnr>").append("11111111111").append("</fnr>");
		builder.append("<fDato>").append("010134").append("</fDato>");
		builder.append("<postnr>").append(1234).append("</postnr>");
		builder.append("<landKd/>");
		builder.append("<spraak>").append("NB").append("</spraak>");
		builder.append("</brevMottaker>");
		builder.append("</brev>");
		builder.append("</rtv-brev>");
		return builder;
	}

	private BrevStatusVO createDefaultBrevstatus() {
		BrevStatusVO brevstatus = new BrevStatusVO();
		brevstatus.setBrevreferanse(BREVREFERANSE);
		brevstatus.setSystemID(BISYS_SYSTEM_ID);
		brevstatus.setReturKoe("svarKo");
		brevstatus.setBestillerBrukerID("B100946");
		brevstatus.setBrevmal("BI01.BI01X01");
		brevstatus.setPassord(SYSTEM_PASSORD);
		return brevstatus;
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
			msg.setJMSCorrelationID(CORRELATION_ID);
			msg.setJMSReplyTo(svarKo);
			if (callId != null) {
				msg.setStringProperty("callId", callId);
			}
			return msg;
		});
	}

}