package no.nav.brevserver.bestillBrev;

import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.xml.bind.JAXBElement;
import no.nav.brevserver.ApplicationTestConfig;
import no.nav.brevserver.core.repository.BrevSystemTilgangRepository;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.repository.BrevtilgangRepository;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.AutoConfigureDataJpa;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT)
@ActiveProfiles("itest")
@EnableWireMock({@ConfigureWireMock(name = "wiremock-server")})
public class AbstractTest {
	protected final String CORRELATION_ID = "abcd-1234-def-5678";
	protected final String CALL_ID = "12-callID-34";
	protected final String SVARKOSTRING = "queue:///mottakSvarKo?targetClient=1";

	@Autowired
	protected JmsTemplate jmsTemplate;
	@Autowired
	protected BrevtilgangRepository brevtilgangRepository;
	@Autowired
	protected BrevSystemTilgangRepository brevSystemTilgangRepository;
	@Autowired
	protected BrevstatusRepository brevstatusRepository;
	@Autowired
	protected BrevstatusService brevstatusService;
	@Autowired
	protected BrevtilgangService brevtilgangService;

	@Autowired
	protected Queue onlinebrev;
	@Autowired
	protected Queue dialogueOnline;
	@Autowired
	protected Queue deadletter;
	@Autowired
	protected Queue onlinebrevPe;
	@Autowired
	protected Queue dialogueOnlinePe;
	@Autowired
	protected Queue deadletterPe;
	@Autowired
	protected Queue mottakSvarKo;

	public void cleanupDb(){
		brevtilgangRepository.deleteAll();
		brevSystemTilgangRepository.deleteAll();
		brevstatusRepository.deleteAll();
	}
	
	protected  <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement<?>) response).getValue();
		}
		return (T) response;
	}

	protected <T> T receive(String queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement<?>) response).getValue();
		}
		return (T) response;
	}

	protected void sendStringMessage(Queue queue, final String message, final String callId) {
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