package no.nav.brevserver.bestillBrev.pesys;

import no.nav.brevserver.bestillBrev.AbstractTest;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.brevserver.bestillBrev.Utils.PENSJON_SYSTEM_ID;
import static no.nav.brevserver.bestillBrev.Utils.createInputFromFagsystem;
import static no.nav.brevserver.bestillBrev.Utils.getBrevFinnesAlleredeString;
import static no.nav.brevserver.bestillBrev.Utils.getHappyPathText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class PeBestillBrevServiceTest extends AbstractTest {
	
	private final String CORRELATION_ID = "abcd-1234-def-5678";
	private final String CALL_ID = "12-callID-34";
	private final String SVARKOSTRING = "queue:///mottakSvarKo?targetClient=1";

	@Test
	public void shouldHandleMessage()  {
		String header = createInputFromFagsystem(PENSJON_SYSTEM_ID);
		sendStringMessage(onlinebrevPe, header, CORRELATION_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(dialogueOnlinePe);
			assertThat(received).isEqualTo(getHappyPathText(PENSJON_SYSTEM_ID));
		});
	}

	@Test
	public void shouldFailOnNullInput() {
		sendStringMessage(onlinebrevPe, null, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			ActiveMQMessage received = receive(deadletterPe);
			assertThat(received.getJMSCorrelationID()).isEqualTo(CORRELATION_ID);
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {"","<rtv-brev>badXML<rtv-brev>", "BI12"})
	public void shouldSendMessageToDeadletterWhenBadInput(String input)  {
		String header = createInputFromFagsystem(input);
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(deadletterPe);
			assertThat(received).isEqualTo(header);
		});
	}

	@Test
	public void brevFinnesAllerede()  {
		String header = createInputFromFagsystem(PENSJON_SYSTEM_ID);
		sendStringMessage(onlinebrevPe, header, CALL_ID);
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOSTRING);
			assertThat(received).isEqualTo(getBrevFinnesAlleredeString(PENSJON_SYSTEM_ID));
		});
	}

}