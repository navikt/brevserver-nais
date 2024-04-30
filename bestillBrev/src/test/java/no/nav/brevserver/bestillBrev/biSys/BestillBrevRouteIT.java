package no.nav.brevserver.bestillBrev.biSys;

import config.AbstractTest;
import no.nav.brevserver.bestillBrev.Utils;
import no.nav.brevserver.core.vo.BrevStatusVO;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.brevserver.bestillBrev.Utils.BISYS_SYSTEM_ID;
import static no.nav.brevserver.bestillBrev.Utils.createInputFromFagsystem;
import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_BREVPAKKE;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BestillBrevRouteIT extends AbstractTest {

	private final String BREVREF_XML = "3835845842";

	@AfterEach
	public void cleanUp() {
		super.cleanupDb();
	}

	@Test
	public void shouldBestillNewBrev() throws Exception {
		String message = Utils.classpathToString("brevXml/bisysBrev.xml");
		sendStringMessage(onlinebrev, message, Utils.CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(dialogueOnline);
			assertNotNull(recieved);
			BrevStatusVO endretBrevstatusVo = brevstatusService.hentBrevStatus(BREVREF_XML, Utils.BISYS_SYSTEM_ID);
			assertEquals(BREVSTATUS_BREVPAKKE, endretBrevstatusVo.getStatus());
		});

	}

	@Test
	public void shouldGiTilgang() throws Exception {
		String message = Utils.classpathToString("brevXml/fraBrevlager.xml");
		sendStringMessage(onlinebrev, message, Utils.CALLID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
				assertTrue(brevtilgangService.sjekkTilgang("BI12", "92fa00f8d8024b0", "klientToken")));

	}

	@Test
	public void shouldSendToFeilKoOnException() throws Exception {
		String badHeader = Utils.classpathToString("brevXml/pensjonsbrev.xml");
		sendStringMessage(onlinebrev, badHeader, Utils.CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletter);
			assertEquals(recieved, Utils.classpathToString("brevXml/pensjonsbrev.xml"));
		});
	}

	@Test
	public void shouldHandleMessage() {
		String header = createInputFromFagsystem(BISYS_SYSTEM_ID);
		sendStringMessage(onlinebrev, header, CORRELATION_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(dialogueOnline);
			assertEquals(received, Utils.getHappyPathText(BISYS_SYSTEM_ID));
		});
	}

	@Test
	public void shouldSaveTilgangWhenFromBrevlager() {
		String header = createInputFromFagsystem(BISYS_SYSTEM_ID, "frabrevlager");
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			assertNotNull(brevtilgangRepository.findBySystemIdAndBrevreferanse("BI12", "10000000000"));
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "<rtv-brev>badXML<rtv-brev>", "PE01"})
	public void shouldSendMessageToDeadletterWhenBadInput(String input) {
		String header = createInputFromFagsystem(input);
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
		String header = createInputFromFagsystem(BISYS_SYSTEM_ID);
		sendStringMessage(onlinebrev, header, CALL_ID);
		sendStringMessage(onlinebrev, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOSTRING);
			assertEquals(received, Utils.getBrevFinnesAlleredeString(BISYS_SYSTEM_ID));

		});
	}

}