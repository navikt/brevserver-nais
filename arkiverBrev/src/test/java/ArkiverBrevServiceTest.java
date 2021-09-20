import config.ApplicationTestConfig;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import org.apache.activemq.command.ActiveMQTextMessage;
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

import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_FEIL;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_BREV_EKSISTERER;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.when;
import static utils.Utils.*;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class})
@ActiveProfiles("itest")
@DirtiesContext
public class ArkiverBrevServiceTest {

	@Inject
	private Queue mottakArkiv;
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

	@Test
	public void shouldSaveAsKladd() throws Exception{
		when(brevstatusServiceMock.hentBrevStatus(BREVREFERANSE, BISYS_SYSTEM_ID)).thenReturn(createDefaultBrevstatus());
		when(brevlagerServiceMock.lagreBrev(any(BrevVO.class), any(BrevStatusVO.class))).thenReturn(null);
		String header = createBisysKvittering(FilType.XML.getJoarkCode());
		sendStringMessage(mottakArkiv, header, "Dette-er-en-callId");

		await().atMost(100, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(svarKo);
			assertEquals(recieved, createReplyToBisysKvittering(STATUS_LAGRET, BISYS_SYSTEM_ID, FILTYPE_XML, "0"));
			verify(brevlagerServiceMock, times(1));
		});
	}

	@Test
	public void shouldFailBrevFinnesAllerede() throws BrevTechnicalException {

		when(brevstatusServiceMock.hentBrevStatus(BREVREFERANSE, BISYS_SYSTEM_ID)).thenReturn(createBrevStatusVOFinnesAllerede());
		when(brevlagerServiceMock.lagreBrev(any(BrevVO.class), any(BrevStatusVO.class))).thenReturn(null);
		String message = createBisysKvittering(FilType.PDF.getJoarkCode());
		sendStringMessage(mottakArkiv, message, "Dette-er-en-callId");

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(svarKo);
			assertEquals(recieved, createReplyToBisysKvittering(BREVSTATUS_FEIL, BISYS_SYSTEM_ID, FORMAT, FEIL_BREV_EKSISTERER));
			verifyZeroInteractions(brevlagerServiceMock);
			verify(brevstatusServiceMock, times(1));
		});
	}

	@Test
	public void shouldFailOnPeFagsystem(){
		String message = createPesysKvittering(FilType.PDF.getJoarkCode());
		sendStringMessage(mottakArkiv, message, "Dette-er-en-callId");

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletter);
			assertEquals(recieved, message);
			verifyZeroInteractions(brevlagerServiceMock, brevstatusServiceMock);
		});
	}


	@Test
	public void shouldFailOnNullKvittering(){
		sendStringMessage(mottakArkiv, null, "Dette-er-en-callId");

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			Object recieved = receive(deadletter);
			//assertEquals(recieved, message);
			verifyZeroInteractions(brevlagerServiceMock, brevstatusServiceMock);
		});
	}

	private BrevStatusVO createDefaultBrevstatus() {
		BrevStatusVO brevstatus = new BrevStatusVO();
		brevstatus.setStatus(Konstanter.BREVSTATUS_BREVPAKKE);
		return brevstatus;
	}

	private <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement) response).getValue();
		}
		return (T) response;
	}

	private String createReplyToBisysKvittering(String status, String fagsystem, String format, String feilkode){
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n");
		builder.append("<rtv-brevkvitt>\n");
		builder.append("<brevref>").append(BREVREFERANSE).append("</brevref>\n");
		builder.append("<sysid>").append(fagsystem).append("</sysid>\n");
		builder.append("<type>").append(format).append("</type>\n");
		builder.append("<status>").append(status).append("</status>\n");
		builder.append("<feilkode>").append(feilkode).append("</feilkode>\n");
		builder.append("</rtv-brevkvitt>");
		return builder.toString();
	}

	private BrevStatusVO createBrevStatusVOFinnesAllerede(){
		BrevStatusVO brevstatus = new BrevStatusVO();
		brevstatus.setStatus(Konstanter.BREVSTATUS_BREVPAKKE);
		brevstatus.setStatus(Konstanter.BREVSTATUS_FERDIG);
		return brevstatus;
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
