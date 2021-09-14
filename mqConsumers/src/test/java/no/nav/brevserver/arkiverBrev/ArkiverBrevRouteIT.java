package no.nav.brevserver.arkiverBrev;

import io.micrometer.core.instrument.util.IOUtils;
import no.nav.brevserver.config.AbstractDatabaseTest;
import no.nav.brevserver.config.ApplicationTestConfig;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.repository.RepositoryConfig;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class})
@ActiveProfiles("itest")


//@EnableTransactionManagement
//@AutoConfigureTestDatabase
/*@AutoConfigureTestEntityManager
@EnableJpaRepositories
@Transactional*/
//TODO:  Fjern. Ser ikke mer på problemet nå da det kan hende modulen deles opp
@DirtiesContext
@Transactional
public class ArkiverBrevRouteIT extends AbstractDatabaseTest {

	@Inject
	private Queue mottakArkiv;
	@Inject
	private Queue deadletter;
	@Inject
	private JmsTemplate jmsTemplate;
	@Inject
	private Queue svarKo;
	@Autowired
	private BrevstatusService brevstatusService;
	/*@Inject
	private BrevlagerService brevlagerService;*/

	private final String CALLID = "dette-er-en-callID";


	@Test
	public void shouldHandleMessage() throws Exception{
		BrevStatusVO brevstatus = createBrevstatus(SYSTEM_ID, BREVREFERANSE);
		brevstatusService.lagreBrevStatus(brevstatus);
		BrevStatusVO vo = brevstatusService.hentBrevStatus(BREVREFERANSE, SYSTEM_ID);

		String header = createXmlKvitteringHeader(FilType.PDF.getContentType());
		sendStringMessage(mottakArkiv, header, CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(svarKo);
			assertNotNull(recieved);
			System.out.println("Asserted!");
		});
	}

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

	public static String classpathToString(String classpathResource) throws IOException {
		InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
		return IOUtils.toString(inputStream, UTF_8);
	}
}