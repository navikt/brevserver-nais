import jakarta.jms.Message;
import jakarta.jms.Queue;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.service.BrevstatusService;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import utils.Utils;

import java.util.concurrent.TimeUnit;

import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_FEIL;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_BREV_EKSISTERER;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_UKJENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static utils.Utils.BISYS_SYSTEM_ID;
import static utils.Utils.BREVREFERANSE;
import static utils.Utils.BREVREFERANSE2;
import static utils.Utils.CALLID;
import static utils.Utils.FILTYPE_XML;
import static utils.Utils.FORMAT;
import static utils.Utils.STATUS_FERDIG;
import static utils.Utils.STATUS_LAGRET;
import static utils.Utils.classpathToString;
import static utils.Utils.createBisysKvittering;
import static utils.Utils.createBisysKvittering2;
import static utils.Utils.createBisysKvitteringfeilNiva;
import static utils.Utils.createBrevstatus;
import static utils.Utils.createPesysKvittering;


public class ArkiverBrevRouteIT extends AbstractTest {
	@Autowired
	protected BrevstatusService brevstatusService;
	@Autowired
	protected Queue mottakArkiv;
	@Autowired
	protected Queue deadletter;
	private final String CALL_ID = "1234-callid-5678";
	//Kan ikke bruke selve køen da vi legger på ?targetclient=1 på kønavnet i servicen.
	private static final String SVARKOSTRING = "queue:///mottakSvarKo?targetClient=1";

	@BeforeEach
	public void cleanUp() {
		super.cleanupDb();
	}

	@Test
	//happypath
	public void shouldHandleMessage() throws Exception {
		lagreDefaultBrevStatusVo();
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		String header = Utils.createBisysKvittering();
		sendStringMessage(mottakArkiv, header + "Dette er en pdf".getBytes(), CALLID);
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			Message recieved = jmsTemplate.receive(SVARKOSTRING);
			assertThat(recieved.getJMSCorrelationID().equals(CORRELATION_ID));
			assertThat(brevstatusService.hentBrevStatus(Utils.BREVREFERANSE, BISYS_SYSTEM_ID).getStatus().equals(STATUS_FERDIG));
		});
	}

	@Test
	public void shouldCreateNewBrevStatus() {
		String header = createBisysKvittering2();
		sendStringMessage(mottakArkiv, header + "Dette er en pdf".getBytes(), CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved2 = receive(SVARKOSTRING);
			assertThat(recieved2.equals(classpathToString("svarXml/happySvarko.xml")));
			BrevStatusVO endretBrevstatusVo = brevstatusService.hentBrevStatus(BREVREFERANSE2, BISYS_SYSTEM_ID);
			assertThat(endretBrevstatusVo.getStatus().equals(STATUS_FERDIG));
		});
	}

	@Test
	public void shouldSendToFeilko() {
		String header = Utils.createBadXmlKvitteringHeader();
		sendStringMessage(mottakArkiv, header, CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(deadletter);
			assertThat(recieved.equals(classpathToString("svarXml/deadletterQ.xml")));
		});
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
		String message = createBisysKvittering(FilType.PDF.getContentType());
		sendStringMessage(mottakArkiv, message, CALL_ID);
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

	protected void lagreDefaultBrevStatusVo() throws BrevTechnicalException {
		BrevStatusVO brevstatus = createBrevstatus(BISYS_SYSTEM_ID, Utils.BREVREFERANSE);
		brevstatusService.lagreBrevStatus(brevstatus);
	}
}