import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.xml.bind.JAXBElement;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.TimeUnit;

import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_FEIL;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_BREV_EKSISTERER;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_UKJENT;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static utils.Utils.BISYS_SYSTEM_ID;
import static utils.Utils.BREVREFERANSE;
import static utils.Utils.FILTYPE_XML;
import static utils.Utils.FORMAT;
import static utils.Utils.STATUS_FERDIG;
import static utils.Utils.STATUS_LAGRET;
import static utils.Utils.createBisysKvittering;
import static utils.Utils.createBisysKvitteringfeilNiva;
import static utils.Utils.createPesysKvittering;

@DirtiesContext
public class ArkiverBrevServiceTest extends AbstractTest {

	@Autowired
	private Queue mottakArkiv;
	@Autowired
	private Queue deadletter;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Queue mottakSvarKo;
	@MockBean
	private BrevstatusService brevstatusServiceMock;
	@MockBean
	private BrevlagerService brevlagerServiceMock;

	private final String CORRELATION_ID = "corr-id";
	private final String CALL_ID = "1234-callid-5678";
	private static final String SVARKOSTRING = "queue:///mottakSvarKo?targetClient=1";

	@Test
	public void shouldSaveAsKladd() throws Exception{
		when(brevstatusServiceMock.hentBrevStatus(BREVREFERANSE, BISYS_SYSTEM_ID)).thenReturn(createDefaultBrevstatus());
		when(brevlagerServiceMock.lagreBrev(any(BrevVO.class), any(BrevStatusVO.class))).thenReturn(null);
		String header = createBisysKvittering(FilType.XML.getJoarkCode());
		sendStringMessage(mottakArkiv, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(SVARKOSTRING);
			assertEquals(recieved, createReplyToBisysKvittering(STATUS_LAGRET, BISYS_SYSTEM_ID, FILTYPE_XML, "0"));
		});
	}

	@Test
	public void shouldSaveAsFerdig() throws Exception{
		when(brevstatusServiceMock.hentBrevStatus(BREVREFERANSE, BISYS_SYSTEM_ID)).thenReturn(createDefaultBrevstatus());
		when(brevlagerServiceMock.lagreBrev(any(BrevVO.class), any(BrevStatusVO.class))).thenReturn(null);
		String header = createBisysKvittering(FilType.PDF.getContentType());
		sendStringMessage(mottakArkiv, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(SVARKOSTRING);
			assertEquals(recieved, createReplyToBisysKvittering(STATUS_FERDIG, BISYS_SYSTEM_ID, FilType.PDF.getContentType(), "0"));
		});
	}

	@Test
	public void shouldHandleFeilKvittering() throws Exception{
		when(brevstatusServiceMock.hentBrevStatus(BREVREFERANSE, BISYS_SYSTEM_ID)).thenReturn(createDefaultBrevstatus());
		when(brevlagerServiceMock.lagreBrev(any(BrevVO.class), any(BrevStatusVO.class))).thenReturn(null);
		String header = createBisysKvitteringfeilNiva();
		sendStringMessage(mottakArkiv, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(SVARKOSTRING);
			assertEquals(recieved, createReplyToBisysKvittering(BREVSTATUS_FEIL, BISYS_SYSTEM_ID, FilType.PDF.getContentType(), FEIL_UKJENT));
		});
		verify(brevstatusServiceMock, times(1)).lagreBrevStatus(any(BrevStatusVO.class));
	}

	@Test
	public void shouldFailBrevFinnesAllerede() throws BrevTechnicalException {

		when(brevstatusServiceMock.hentBrevStatus(BREVREFERANSE, BISYS_SYSTEM_ID)).thenReturn(createBrevStatusVOFinnesAllerede());
		when(brevlagerServiceMock.lagreBrev(any(BrevVO.class), any(BrevStatusVO.class))).thenReturn(null);
		String message = createBisysKvittering(FilType.PDF.getJoarkCode());
		sendStringMessage(mottakArkiv, message, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(SVARKOSTRING);
			assertEquals(recieved, createReplyToBisysKvittering(BREVSTATUS_FEIL, BISYS_SYSTEM_ID, FORMAT, FEIL_BREV_EKSISTERER));
			verifyNoInteractions(brevlagerServiceMock);
		});
	}

	@Test
	public void shouldFailOnPeFagsystem(){
		String message = createPesysKvittering();
		sendStringMessage(mottakArkiv, message, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletter);
			assertEquals(recieved, message);
			verifyNoInteractions(brevlagerServiceMock, brevstatusServiceMock);
		});
	}

	@Test
	public void shouldFailOnNullKvittering(){
		sendStringMessage(mottakArkiv, null, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			ActiveMQMessage recieved = receive(deadletter);
			assertEquals(recieved.getJMSCorrelationID(), CORRELATION_ID);
			verifyNoInteractions(brevlagerServiceMock, brevstatusServiceMock);
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

	private <T> T receive(String queue) {
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
			TextMessage msg = session.createTextMessage();
			msg.setText(message);
			msg.setJMSCorrelationID(CORRELATION_ID);
			msg.setJMSReplyTo(mottakSvarKo);
			if (callId != null) {
				msg.setStringProperty("callId", callId);
			}
			return msg;
		});
	}

}
