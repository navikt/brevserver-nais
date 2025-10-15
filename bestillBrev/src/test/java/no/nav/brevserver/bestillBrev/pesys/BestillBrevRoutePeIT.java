package no.nav.brevserver.bestillBrev.pesys;

import config.AbstractTest;
import no.nav.brevserver.core.vo.BrevStatusVO;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.brevserver.bestillBrev.Utils.CALLID;
import static no.nav.brevserver.bestillBrev.Utils.PENSJON_SYSTEM_ID;
import static no.nav.brevserver.bestillBrev.Utils.classpathToString;
import static no.nav.brevserver.bestillBrev.Utils.createInputFromFagsystem;
import static no.nav.brevserver.bestillBrev.Utils.getBrevFinnesAlleredeString;
import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_BREVPAKKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class BestillBrevRoutePeIT extends AbstractTest {

	private final String BREVREF_XML = "3835845842";

	@AfterEach
	public void cleanUp() {
		super.cleanupDb();
	}

	@Test
	public void shouldBestillNewBrev() throws Exception {
		String message = classpathToString("brevXml/pensjonsbrev.xml");
		sendStringMessage(onlinebrevPe, message, CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(dialogueOnlinePe);
			assertThat(received).isNotNull();
			BrevStatusVO endretBrevstatusVo = brevstatusService.hentBrevStatus(BREVREF_XML, PENSJON_SYSTEM_ID);
			assertThat(endretBrevstatusVo.getStatus()).isEqualTo(BREVSTATUS_BREVPAKKE);
		});
	}

	@Test
	public void shouldGiTilgang() throws Exception {
		String message = classpathToString("brevXml/fraBrevlagerPe.xml");
		sendStringMessage(onlinebrevPe, message, CALLID);

		await().atMost(10, SECONDS).untilAsserted(() ->
				assertThat(brevtilgangService.sjekkTilgang(PENSJON_SYSTEM_ID, "92fa00f8d8024b0", "klientToken")).isTrue()
		);
	}

	@Test
	public void shouldSendToFeilKoOnException() throws Exception {
		String badHeader = classpathToString("brevXml/bisysBrev.xml");
		sendStringMessage(onlinebrevPe, badHeader, CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(deadletter);
			assertThat(received).isEqualTo(classpathToString("brevXml/bisysBrev.xml"));
		});
	}

	@Test
	public void shouldSaveTilgangWhenFromBrevlager() {
		String header = createInputFromFagsystem(PENSJON_SYSTEM_ID, "frabrevlager");
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			assertThat(brevtilgangRepository.findBySystemIdAndBrevreferanse("BI12", "10000000000")).isNotNull();
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
			assertThat(received).isEqualTo(header);
		});
	}

	@Test
	public void shouldFailOnNullInput() {
		sendStringMessage(onlinebrevPe, null, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			ActiveMQMessage received = receive(deadletter);
			assertThat(received.getJMSCorrelationID()).isEqualTo(CORRELATION_ID);
		});
	}

	@Test
	public void brevFinnesAllerede() {
		String header = createInputFromFagsystem(PENSJON_SYSTEM_ID);
		sendStringMessage(onlinebrevPe, header, CALL_ID);
		sendStringMessage(onlinebrevPe, header, CALL_ID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOSTRING);
			assertThat(received).isEqualTo(getBrevFinnesAlleredeString(PENSJON_SYSTEM_ID));
		});
	}

}