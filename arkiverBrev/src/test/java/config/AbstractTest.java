package config;

import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.xml.bind.JAXBElement;
import lombok.SneakyThrows;
import no.nav.brevserver.core.repository.BrevRepository;
import no.nav.brevserver.core.repository.BrevSystemTilgangRepository;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.repository.BrevtilgangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
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
public class AbstractTest {
	@Autowired
	protected BrevtilgangRepository brevtilgangRepository;
	@Autowired
	protected BrevSystemTilgangRepository brevSystemTilgangRepository;
	@Autowired
	protected BrevstatusRepository brevstatusRepository;
	@Autowired
	protected BrevRepository brevRepository;
	@Autowired
	protected JmsTemplate jmsTemplate;
	@Autowired
	protected Queue mottakSvarKo;

	protected final String CORRELATION_ID = "corr-id";

	public void cleanupDb() {
		brevtilgangRepository.deleteAll();
		brevSystemTilgangRepository.deleteAll();
		brevstatusRepository.deleteAll();
		brevRepository.deleteAll();
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	protected <T> T receive(String queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement<?>) response).getValue();
		}
		return (T) response;
	}

	protected <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement<?>) response).getValue();
		}
		return (T) response;
	}

	@SneakyThrows
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