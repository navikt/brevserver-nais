package no.nav.brevserver.bestillBrev.pesys;

import config.AbstractTest;
import no.nav.brevserver.bestillBrev.Utils;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.TimeUnit;

import static no.nav.brevserver.bestillBrev.Utils.PENSJON_SYSTEM_ID;
import static no.nav.brevserver.bestillBrev.Utils.createInput;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PeBestillBrevServiceTest extends AbstractTest {
	
	private final String CORRELATION_ID = "abcd-1234-def-5678";
	private final String CALL_ID = "12-callID-34";
	private final String SVARKOSTRING = "queue:///SvarKo?targetClient=1";

	@Test
	public void shouldHandleMessage()  {
		String header = createInput(PENSJON_SYSTEM_ID);
		sendStringMessage(onlinebrevPe, header, CORRELATION_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(dialogueOnlinePe);
			assertEquals(recieved, Utils.getHappyPathText(PENSJON_SYSTEM_ID));
		});
	}

	@Test
	public void shouldFailOnNullInput() {
		sendStringMessage(onlinebrevPe, null, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			ActiveMQMessage recieved = receive(deadletterPe);
			assertEquals(recieved.getJMSCorrelationID(), (CORRELATION_ID));
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {"","<rtv-brev>badXML<rtv-brev>", "BI12"})
	public void shouldSendMessageToDeadletterWhenBadInput(String input)  {
		String header = createInput(input);
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletterPe);
			assertEquals(recieved, header);
		});
	}

	@Test
	public void brevFinnesAllerede()  {
		String header = createInput(PENSJON_SYSTEM_ID);
		sendStringMessage(onlinebrevPe, header, CALL_ID);
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(SVARKOSTRING);
			assertEquals(recieved, Utils.getBrevFinnesAlleredeString(PENSJON_SYSTEM_ID));
		});
	}
}