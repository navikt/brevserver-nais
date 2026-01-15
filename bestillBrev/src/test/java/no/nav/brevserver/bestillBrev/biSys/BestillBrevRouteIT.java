package no.nav.brevserver.bestillBrev.biSys;

import no.nav.brevserver.bestillBrev.AbstractTest;
import no.nav.brevserver.core.vo.BrevStatusVO;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.brevserver.bestillBrev.Utils.BISYS_SYSTEM_ID;
import static no.nav.brevserver.bestillBrev.Utils.CALLID;
import static no.nav.brevserver.bestillBrev.Utils.classpathToString;
import static no.nav.brevserver.bestillBrev.Utils.createInputFromFagsystem;
import static no.nav.brevserver.bestillBrev.Utils.getBrevFinnesAlleredeString;
import static no.nav.brevserver.bestillBrev.Utils.getHappyPathText;
import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_BREVPAKKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class BestillBrevRouteIT extends AbstractTest {

	private final String BREVREF_XML = "3835845842";

	@AfterEach
	public void cleanUp() {
		super.cleanupDb();
	}

	@Test
	public void shouldBestillNewBrev() throws Exception {
		String message = classpathToString("brevXml/bisysBrev.xml");
		sendStringMessage(onlinebrev, message, CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(dialogueOnline);
			assertThat(received).isNotNull();
			BrevStatusVO endretBrevstatusVo = brevstatusService.hentBrevStatus(BREVREF_XML, BISYS_SYSTEM_ID);
			assertThat(endretBrevstatusVo.getStatus()).isEqualTo(BREVSTATUS_BREVPAKKE);
		});
	}

	@Test
	public void shouldGiTilgang() throws Exception {
		String message = classpathToString("brevXml/fraBrevlager.xml");
		sendStringMessage(onlinebrev, message, CALLID);

		await().atMost(10, SECONDS).untilAsserted(() ->
				assertThat(brevtilgangService.sjekkTilgang("BI12", "92fa00f8d8024b0", "klientToken")).isTrue()
		);
	}

	@Test
	public void shouldSendToFeilKoOnException() throws Exception {
		String badHeader = classpathToString("brevXml/pensjonsbrev.xml");
		sendStringMessage(onlinebrev, badHeader, CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(deadletter);
			assertThat(received).isEqualTo(classpathToString("brevXml/pensjonsbrev.xml"));
		});
	}

	@Test
	public void shouldHandleMessage() {
		String header = createInputFromFagsystem(BISYS_SYSTEM_ID);
		sendStringMessage(onlinebrev, header, CORRELATION_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(dialogueOnline);
			assertThat(received).isEqualTo(getHappyPathText(BISYS_SYSTEM_ID));
		});
	}

	@Test
	public void shouldSaveTilgangWhenFromBrevlager() {
		String header = createInputFromFagsystem(BISYS_SYSTEM_ID, "frabrevlager");
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() ->
				assertThat(brevtilgangRepository.findBySystemIdAndBrevreferanse("BI12", "10000000000")).isNotNull());
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "<rtv-brev>badXML<rtv-brev>", "PE01"})
	public void shouldSendMessageToDeadletterWhenBadInput(String input) {
		String header = createInputFromFagsystem(input);
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(deadletter);
			assertThat(received).isEqualTo(header);
		});
	}

	@Test
	public void shouldFailOnNullInput() {
		sendStringMessage(onlinebrev, null, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			ActiveMQMessage received = receive(deadletter);
			assertThat(received.getJMSCorrelationID()).isEqualTo(CORRELATION_ID);
		});
	}

	@Test
	public void brevFinnesAllerede() {
		String header = createInputFromFagsystem(BISYS_SYSTEM_ID);
		sendStringMessage(onlinebrev, header, CALL_ID);
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOSTRING);
			assertThat(received).isEqualTo(getBrevFinnesAlleredeString(BISYS_SYSTEM_ID));
		});
	}

}