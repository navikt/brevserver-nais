package no.nav.brevserver.bestillBrev.pesys;

import config.ApplicationTestConfig;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static no.nav.brevserver.bestillBrev.Utils.BREVREFERANSE;
import static no.nav.brevserver.bestillBrev.Utils.SYSTEM_PASSORD;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyZeroInteractions;


@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class})
@ActiveProfiles("itest")
@DirtiesContext
public class PeBestillBrevServiceTest {


	@Inject
	private Queue onlinebrevPe;
	@Inject
	private Queue deadletterPe;
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
	private final String SYSTEM_ID = "PE2";
	private final String BREVMAL = "PE_AP_04_220";

	@Test
	public void shouldFailOnNullInput(){
		sendStringMessage(onlinebrevPe, null, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			ActiveMQMessage recieved = receive(deadletterPe);
			assertEquals(recieved.getJMSCorrelationID(), (CORRELATION_ID));
			verifyZeroInteractions(brevtilgangServiceMock, brevstatusServiceMock);
		});
	}


	/*
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

	private String createInput(){
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		builder.append("<rtv-brev direkteutskrift=\"NEI\" format=\"ENSIDIG\" malpakke=\"BI01.BI01X01\" sysid=\"").append(PENSJON_SYSTEM_ID).append("\" passord=\"Bisys123\" saksbehandler=\"B100946\">");
		addText(builder);
		return builder.toString();
	}
*/
	private String createXmlKvitteringHeader(String contentType) {
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
		builder.append("<rtv-brevkvitt>");
		builder.append("<brevref>").append(BREVREFERANSE).append("</brevref>");
		builder.append("<sysid>").append(SYSTEM_ID).append("</sysid>");
		builder.append("<type>").append(contentType).append("</type>");
		builder.append("<feilniva>").append("0").append("</feilniva>");
		builder.append("<feilkode>").append("0").append("</feilkode>");
		builder.append("</rtv-brevkvitt>");
		return StringUtils.rightPad(builder.toString(), Konstanter.MELDING_HEADER_LENGTH, ' ');
	}

	private BrevStatusVO createDefaultBrevstatus() {
		BrevStatusVO brevstatus = new BrevStatusVO();
		brevstatus.setBrevreferanse(BREVREFERANSE);
		brevstatus.setSystemID(SYSTEM_ID);
		brevstatus.setReturKoe("svarKo");
		brevstatus.setBestillerBrukerID("B100946");
		brevstatus.setBrevmal(BREVMAL);
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