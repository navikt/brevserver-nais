package no.nav.brevserver.bestillBrev.pesys;

import config.AbstractTest;
import no.nav.brevserver.bestillBrev.Utils;
import no.nav.brevserver.core.vo.BrevStatusVO;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.brevserver.bestillBrev.Utils.PENSJON_SYSTEM_ID;
import static no.nav.brevserver.bestillBrev.Utils.classpathToString;
import static no.nav.brevserver.bestillBrev.Utils.createInputFromFagsystem;
import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_BREVPAKKE;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BestillBrevRoutePeIT extends AbstractTest {

	private final String BREVREF_XML = "3835845842";

	@AfterEach
	public void cleanUp() {
		super.cleanupDb();
	}

	@Test
	public void shouldBestillNewBrev() throws Exception {
		String message = classpathToString("brevXml/pensjonsbrev.xml");
		sendStringMessage(onlinebrevPe, message, Utils.CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(dialogueOnlinePe);
			assertNotNull(recieved);
			BrevStatusVO endretBrevstatusVo = brevstatusService.hentBrevStatus(BREVREF_XML, PENSJON_SYSTEM_ID);
			assertEquals(BREVSTATUS_BREVPAKKE, endretBrevstatusVo.getStatus());
		});

	}

	@Test
	public void shouldGiTilgang() throws Exception {
		String message = classpathToString("brevXml/fraBrevlagerPe.xml");
		sendStringMessage(onlinebrevPe, message, Utils.CALLID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
				assertTrue(brevtilgangService.sjekkTilgang(PENSJON_SYSTEM_ID, "92fa00f8d8024b0", "klientToken")));

	}

	@Test
	public void shouldSendToFeilKoOnException() throws Exception {
		String badHeader = classpathToString("brevXml/bisysBrev.xml");
		sendStringMessage(onlinebrevPe, badHeader, Utils.CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletter);
			assertEquals(recieved, classpathToString("brevXml/bisysBrev.xml"));
		});
	}

	@Test
	public void shouldSaveTilgangWhenFromBrevlager() {
		String header = createInputFromFagsystem(PENSJON_SYSTEM_ID, "frabrevlager");
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			assertNotNull(brevtilgangRepository.findBySystemIdAndBrevreferanse("BI12", "10000000000"));
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "<rtv-brev>badXML<rtv-brev>", "BI12"})
	@NullSource
	public void shouldSendMessageToDeadletterWhenBadFagsystem(String fagsystem) {
		String header = createInputFromFagsystem(fagsystem);
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(deadletter);
			assertEquals(received, header);
		});
	}

	@Test
	public void shouldFailOnNullInput() {
		sendStringMessage(onlinebrevPe, null, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			ActiveMQMessage received = receive(deadletter);
			assertEquals(received.getJMSCorrelationID(), (CORRELATION_ID));
		});
	}

	@Test
	public void brevFinnesAllerede() {
		String header = createInputFromFagsystem(PENSJON_SYSTEM_ID);
		sendStringMessage(onlinebrevPe, header, CALL_ID);
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOSTRING);
			assertEquals(received, Utils.getBrevFinnesAlleredeString(PENSJON_SYSTEM_ID));
		});
	}

}