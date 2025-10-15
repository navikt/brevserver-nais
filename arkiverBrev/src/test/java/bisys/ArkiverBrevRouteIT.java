package bisys;

import config.AbstractTest;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import no.nav.brevserver.service.BrevstatusService;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import utils.Utils;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_FEIL;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_BREV_EKSISTERER;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_UKJENT;
import static no.nav.brevserver.core.vo.FilType.PDF;
import static no.nav.brevserver.core.vo.FilType.XML;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static utils.Utils.BISYS_SYSTEM_ID;
import static utils.Utils.BREVREFERANSE;
import static utils.Utils.CALLID;
import static utils.Utils.FILTYPE_XML;
import static utils.Utils.FORMAT;
import static utils.Utils.PDF_CONTENTTYPE;
import static utils.Utils.STATUS_FERDIG;
import static utils.Utils.STATUS_LAGRET;
import static utils.Utils.classpathToString;
import static utils.Utils.createBadXmlKvitteringHeader;
import static utils.Utils.createBisysKvittering;
import static utils.Utils.createBisysKvitteringfeilNiva;
import static utils.Utils.createPesysKvittering;

public class ArkiverBrevRouteIT extends AbstractTest {

	private final String CALL_ID = "1234-callid-5678";
	//Kan ikke bruke selve køen da vi legger på ?targetclient=1 på kønavnet i servicen.
	private static final String SVARKOE = "queue:///mottakSvarKo?targetClient=1";

	@Autowired
	protected BrevstatusService brevstatusService;
	@Autowired
	protected Queue mottakArkiv;
	@Autowired
	protected Queue deadletter;

	@BeforeEach
	public void cleanUp() {
		super.cleanupDb();
	}

	@Test
	public void shouldArkivereBrev() {
		String header = createBisysKvittering();
		sendStringMessage(mottakArkiv, header + "Dette er en pdf".getBytes(), CALLID);

		await().atMost(5, SECONDS).untilAsserted(() -> {
			Message received = jmsTemplate.receive(SVARKOE);
			assertThat(received).isNotNull()
					.satisfies(receivedMessage -> {
						assertThat(received.getJMSCorrelationID()).isEqualTo(CORRELATION_ID);
						assertThat(received.getBody(String.class)).isEqualTo(createReplyToBisysKvittering(STATUS_FERDIG, BISYS_SYSTEM_ID, PDF_CONTENTTYPE, "0"));
						assertThat(brevstatusService.hentBrevStatus(Utils.BREVREFERANSE, BISYS_SYSTEM_ID).getStatus()).isEqualTo(STATUS_FERDIG);
					});
		});
	}

	@Test
	public void shouldSendToFeilkoe() {
		String header = createBadXmlKvitteringHeader(BISYS_SYSTEM_ID);
		sendStringMessage(mottakArkiv, header, CALLID);

		await().atMost(5, SECONDS).untilAsserted(() -> {
			String received = receive(deadletter);
			assertThat(received).isEqualTo(classpathToString("svarXml/deadletterQ.xml"));
		});
	}

	@Test
	public void shouldSaveAsKladd() {
		String header = createBisysKvittering(XML.getJoarkCode());
		sendStringMessage(mottakArkiv, header, CALL_ID);

		await().atMost(5, SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOE);
			assertThat(received).isEqualTo(createReplyToBisysKvittering(STATUS_LAGRET, BISYS_SYSTEM_ID, FILTYPE_XML, "0"));
		});
	}

	@Test
	public void shouldSaveAsFerdig() {
		String header = createBisysKvittering(PDF.getContentType());
		sendStringMessage(mottakArkiv, header, CALL_ID);

		await().atMost(5, SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOE);
			assertThat(received).isEqualTo(createReplyToBisysKvittering(STATUS_FERDIG, BISYS_SYSTEM_ID, PDF.getContentType(), "0"));
		});
	}

	@Test
	public void shouldHandleFeilKvittering() {
		String header = createBisysKvitteringfeilNiva();
		sendStringMessage(mottakArkiv, header, CALL_ID);

		await().atMost(5, SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOE);
			assertThat(received).isEqualTo(createReplyToBisysKvittering(BREVSTATUS_FEIL, BISYS_SYSTEM_ID, PDF.getContentType(), FEIL_UKJENT));
		});
	}

	@Test
	public void shouldFailBrevFinnesAllerede() {
		String message = createBisysKvittering(PDF.getContentType());
		sendStringMessage(mottakArkiv, message, CALL_ID);
		sendStringMessage(mottakArkiv, message, CALL_ID);

		await().atMost(5, SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOE);
			assertThat(received).isEqualTo(createReplyToBisysKvittering(BREVSTATUS_FEIL, BISYS_SYSTEM_ID, FORMAT, FEIL_BREV_EKSISTERER));
		});
	}

	@Test
	public void shouldFailOnPeFagsystem() {
		String message = createPesysKvittering();
		sendStringMessage(mottakArkiv, message, CALL_ID);

		await().atMost(5, SECONDS).untilAsserted(() -> {
			String received = receive(deadletter);
			assertThat(received).isEqualTo(message);
		});
	}

	@Test
	public void shouldFailOnNullKvittering() {
		sendStringMessage(mottakArkiv, null, CALL_ID);

		await().atMost(5, SECONDS).untilAsserted(() -> {
			ActiveMQMessage received = receive(deadletter);
			assertThat(received.getJMSCorrelationID()).isEqualTo(CORRELATION_ID);
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