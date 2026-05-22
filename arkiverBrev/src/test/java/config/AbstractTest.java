package config;

import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.xml.bind.JAXBElement;
import lombok.SneakyThrows;
import no.nav.brevserver.ApplicationTestConfig;
import no.nav.brevserver.core.repository.BrevRepository;
import no.nav.brevserver.core.repository.BrevSystemTilgangRepository;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.repository.BrevtilgangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.AutoConfigureDataJpa;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.EnableWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT)
@ActiveProfiles("itest")
@EnableWireMock
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

	protected static void naisTexasTokenStub() {
		stubFor(post("/naistexas")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("naistexas/token_response.json")));
	}

	protected static void dokarkivStub() {
		dokarkivStub(OK, "dokarkiv/settbrevdata-ok.json");
	}

	protected static void dokarkivStubServerError() {
		dokarkivStub(INTERNAL_SERVER_ERROR, "dokarkiv/settbrevdata-server-error.json");
	}

	protected static void dokarkivStub(HttpStatus httpStatus, String bodyFile) {
		stubFor(post(urlPathMatching("/dokarkiv/journalpostapi/v1/journalpost/(\\d+)/settBrevdata/(ARKIV|PRODUKSJON)"))
				.willReturn(aResponse()
						.withStatus(httpStatus.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(bodyFile)));
	}


}
