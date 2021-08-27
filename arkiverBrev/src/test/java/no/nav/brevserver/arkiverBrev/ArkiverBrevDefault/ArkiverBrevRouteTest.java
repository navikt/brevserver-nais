package no.nav.brevserver.arkiverBrev.ArkiverBrevDefault;

import io.micrometer.core.instrument.util.IOUtils;
import no.nav.brevserver.arkiverBrev.config.ApplicationTestConfig;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class})
@ActiveProfiles("itest")
public class ArkiverBrevRouteTest {

	@Inject
	private Queue mottakArkiv;
	@Inject
	private Queue mottakOnline;
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


	//Test for å gjøre det lettere å lage routen riktig
	@Test
	public void shouldHandleMessage() throws Exception{
		when(brevstatusServiceMock.hentBrevStatus("systemId","brevReferanse")).thenReturn(createDefaultBrevstatus());
		when(brevlagerServiceMock.lagreBrev(any(BrevVO.class), any(BrevStatusVO.class))).thenReturn(null);
		System.out.println();
		String header = createXmlKvitteringHeader(FilType.PDF.getContentType())+ "some pdf-content";
		System.out.println(header);
		sendStringMessage(mottakArkiv, header, "Dette-er-en-callId");
		await().atMost(120, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(svarKo);
			assertNotNull(recieved);
		});
	}

	private BrevStatusVO createDefaultBrevstatus() {
		BrevStatusVO brevstatus = new BrevStatusVO();
		brevstatus.setStatus(Konstanter.BREVSTATUS_BREVPAKKE);
		return brevstatus;
	}

	@Test
	public void test(){
		String restult = createXmlKvitteringHeader(FilType.PDF.getContentType());
		System.out.println(restult);
	}

	private String createXmlKvitteringHeader(String contentType) {
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
		builder.append("<rtv-brevkvitt>");
		builder.append("<brevref>").append("brevReferanse").append("</brevref>");
		builder.append("<sysid>").append("systemId").append("</sysid>");
		builder.append("<type>").append(contentType).append("</type>");
		builder.append("<feilniva>").append("0").append("</feilniva>");
		builder.append("<feilkode>").append("0").append("</feilkode>");
		builder.append("</rtv-brevkvitt>");
		return StringUtils.rightPad(builder.toString(), Konstanter.MELDING_HEADER_LENGTH, ' ');
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

	public static String classpathToString(String classpathResource) throws IOException {
		InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
		return IOUtils.toString(inputStream, UTF_8);
	}
}