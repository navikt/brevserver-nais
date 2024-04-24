import jakarta.jms.Queue;
import no.nav.brevserver.core.vo.FilType;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_FEIL;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_BREV_EKSISTERER;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_UKJENT;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static utils.Utils.BISYS_SYSTEM_ID;
import static utils.Utils.BREVREFERANSE;
import static utils.Utils.FILTYPE_XML;
import static utils.Utils.FORMAT;
import static utils.Utils.STATUS_FERDIG;
import static utils.Utils.STATUS_LAGRET;
import static utils.Utils.createBisysKvittering;
import static utils.Utils.createBisysKvitteringfeilNiva;
import static utils.Utils.createPesysKvittering;

public class ArkiverBrevServiceTest extends AbstractTest {

	@Autowired
	private Queue mottakArkiv;
	@Autowired
	private Queue deadletter;

	private final String CORRELATION_ID = "corr-id";
	private final String CALL_ID = "1234-callid-5678";
	private static final String SVARKOSTRING = "queue:///mottakSvarKo?targetClient=1";

	@AfterEach
	public void cleanUp(){
		super.cleanupDb();
	}

	@Test
	public void shouldSaveAsKladd() {
		String header = createBisysKvittering(FilType.XML.getJoarkCode());
		sendStringMessage(mottakArkiv, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(SVARKOSTRING);
			assertEquals(recieved, createReplyToBisysKvittering(STATUS_LAGRET, BISYS_SYSTEM_ID, FILTYPE_XML, "0"));
		});
	}

	@Test
	public void shouldSaveAsFerdig() {
		String header = createBisysKvittering(FilType.PDF.getContentType());
		sendStringMessage(mottakArkiv, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(SVARKOSTRING);
			assertEquals(recieved, createReplyToBisysKvittering(STATUS_FERDIG, BISYS_SYSTEM_ID, FilType.PDF.getContentType(), "0"));
		});
	}

	@Test
	public void shouldHandleFeilKvittering() {
		String header = createBisysKvitteringfeilNiva();
		sendStringMessage(mottakArkiv, header, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(SVARKOSTRING);
			assertEquals(recieved, createReplyToBisysKvittering(BREVSTATUS_FEIL, BISYS_SYSTEM_ID, FilType.PDF.getContentType(), FEIL_UKJENT));
		});
	}

	@Test
	public void shouldFailBrevFinnesAllerede() {
		String message = createBisysKvittering(FilType.PDF.getJoarkCode());
		sendStringMessage(mottakArkiv, message, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(SVARKOSTRING);
			assertEquals(recieved, createReplyToBisysKvittering(BREVSTATUS_FEIL, BISYS_SYSTEM_ID, FORMAT, FEIL_BREV_EKSISTERER));
		});
	}

	@Test
	public void shouldFailOnPeFagsystem() {
		String message = createPesysKvittering();
		sendStringMessage(mottakArkiv, message, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletter);
			assertEquals(recieved, message);
		});
	}

	@Test
	public void shouldFailOnNullKvittering() {
		sendStringMessage(mottakArkiv, null, CALL_ID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			ActiveMQMessage recieved = receive(deadletter);
			assertEquals(recieved.getJMSCorrelationID(), CORRELATION_ID);
		});
	}

	private String createReplyToBisysKvittering(String status, String fagsystem, String format, String feilkode) {
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n");
		builder.append("<rtv-brevkvitt>\n");
		builder.append("<brevref>").append(BREVREFERANSE).append("</brevref>\n");
		builder.append("<sysid>").append(fagsystem).append("</sysid>\n");
		builder.append("<type>").append(format).append("</type>\n");
		builder.append("<status>").append(status).append("</status>\n");
		builder.append("<feilkode>").append(feilkode).append("</feilkode>\n");
		builder.append("</rtv-brevkvitt>");
		return builder.toString();
	}
}
