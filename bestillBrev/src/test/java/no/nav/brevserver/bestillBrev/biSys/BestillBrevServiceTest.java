package no.nav.brevserver.bestillBrev.biSys;

import config.AbstractTest;
import no.nav.brevserver.bestillBrev.Utils;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.brevserver.bestillBrev.Utils.BISYS_SYSTEM_ID;
import static no.nav.brevserver.bestillBrev.Utils.createInput;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BestillBrevServiceTest extends AbstractTest {

	@AfterEach
	public void cleanUp(){
		cleanupDb();
	}

	@Test
	public void shouldHandleMessage() {
		String header = createInput(BISYS_SYSTEM_ID);
		sendStringMessage(onlinebrev, header, CORRELATION_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(dialogueOnline);
			assertEquals(received, Utils.getHappyPathText(BISYS_SYSTEM_ID));
		});
	}

	@Test
	public void shouldSaveTilgangWhenFromBrevlager() {
		String header = createInput(BISYS_SYSTEM_ID, "frabrevlager");
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			assertNotNull(brevtilgangRepository.findBySystemIdAndBrevreferanse("BI12", "10000000000"));
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {"","<rtv-brev>badXML<rtv-brev>", "PE01"})
	public void shouldSendMessageToDeadletterWhenBadInput(String input) {
		String header = createInput(input);
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(deadletter);
			assertEquals(received, header);
		});
	}

	@Test
	public void shouldFailOnNullInput() {
		sendStringMessage(onlinebrev, null, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			ActiveMQMessage received = receive(deadletter);
			assertEquals(received.getJMSCorrelationID(), (CORRELATION_ID));
		});
	}

	@Test
	public void brevFinnesAllerede() {
		String header = createInput(BISYS_SYSTEM_ID);
		sendStringMessage(onlinebrev, header, CALL_ID);
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOSTRING);
			assertEquals(received, Utils.getBrevFinnesAlleredeString(BISYS_SYSTEM_ID));

		});
	}
}