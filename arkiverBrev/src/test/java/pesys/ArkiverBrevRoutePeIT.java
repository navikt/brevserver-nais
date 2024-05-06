package pesys;

import config.AbstractTest;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.joark.JoarkService;
import no.nav.brevserver.service.BrevstatusService;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import utils.Utils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_FEIL;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_BREV_EKSISTERER;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_UKJENT;
import static no.nav.brevserver.core.vo.FilType.PDF;
import static no.nav.brevserver.core.vo.FilType.RTF;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static utils.Utils.BREVREFERANSE;
import static utils.Utils.CALLID;
import static utils.Utils.FORMAT;
import static utils.Utils.PDF_CONTENTTYPE;
import static utils.Utils.PENSJON_SYSTEM_ID;
import static utils.Utils.STATUS_FERDIG;
import static utils.Utils.STATUS_LAGRET;
import static utils.Utils.classpathToString;
import static utils.Utils.createBisysKvittering;
import static utils.Utils.createPesysKvittering;
import static utils.Utils.createPesysKvitteringFeilNiva;


public class ArkiverBrevRoutePeIT extends AbstractTest {
	@Autowired
	protected BrevstatusService brevstatusService;
	@Autowired
	protected Queue mottakArkivPeLinux;
	@Autowired
	protected Queue deadletter;
	@Autowired
	private Queue mottakArkivPeLinuxBq;
	@Autowired
	private JoarkService joarkServiceMock;

	private final String CALL_ID = "1234-callid-5678";
	//Kan ikke bruke selve køen da vi legger på ?targetclient=1 på kønavnet i servicen.
	private static final String SVARKOSTRING = "queue:///mottakSvarKo?targetClient=1";


	@BeforeEach
	public void cleanUp() {
		super.cleanupDb();
	}

	@Test
	//happypath
	public void shouldArkivereBrev() {
		String header = Utils.createPesysKvittering();
		sendStringMessage(mottakArkivPeLinux, header + "Dette er en pdf".getBytes(), CALLID);
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			Message received = jmsTemplate.receive(SVARKOSTRING);
			assertEquals(Objects.requireNonNull(received).getJMSCorrelationID(), CORRELATION_ID);
			assertEquals(received.getBody(String.class), createReplyToKvittering(STATUS_FERDIG, PENSJON_SYSTEM_ID, PDF_CONTENTTYPE, "0"));
			assertEquals(brevstatusService.hentBrevStatus(Utils.BREVREFERANSE, PENSJON_SYSTEM_ID).getStatus(), STATUS_FERDIG);
		});
	}

	@Test
	public void shouldSendToFeilko() {
		String header = Utils.createBadXmlKvitteringHeader(PENSJON_SYSTEM_ID);
		sendStringMessage(mottakArkivPeLinux, header, CALLID);
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String received = receive(deadletter);
			assertEquals(received, classpathToString("svarXml/deadletterPe.xml"));
		});
	}

	@Test
	public void shouldSendMessageToBqWhenBrevTechnicalException() throws BrevException {
		doThrow(BrevTechnicalException.class).when(joarkServiceMock).lagreDokument(any(), any(), any());
		String message = createPesysKvittering();
		sendStringMessage(mottakArkivPeLinux, message, CALLID);
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String received = receive(mottakArkivPeLinuxBq);
			assertEquals(received, message);
		});
	}

	@Test
	public void shouldSaveAsKladd() {
		String header = createPesysKvittering(RTF.getContentType());
		sendStringMessage(mottakArkivPeLinux, header, CALL_ID);

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOSTRING);
			assertEquals(received, createReplyToKvittering(STATUS_LAGRET, PENSJON_SYSTEM_ID, RTF.getContentType(), "0"));
		});
	}

	@Test
	public void shouldSaveAsFerdig() {
		String header = createPesysKvittering(PDF.getContentType());
		sendStringMessage(mottakArkivPeLinux, header, CALL_ID);

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOSTRING);
			assertEquals(received, createReplyToKvittering(STATUS_FERDIG, PENSJON_SYSTEM_ID, PDF.getContentType(), "0"));
		});
	}

	@Test
	public void shouldHandleFeilKvittering() {
		String header = createPesysKvitteringFeilNiva();
		sendStringMessage(mottakArkivPeLinux, header, CALL_ID);

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOSTRING);
			assertEquals(received, createReplyToKvittering(BREVSTATUS_FEIL, PENSJON_SYSTEM_ID, PDF.getContentType(), FEIL_UKJENT));
		});
	}

	@Test
	public void shouldFailBrevFinnesAllerede() {
		String message = createPesysKvittering();
		sendStringMessage(mottakArkivPeLinux, message, CALL_ID);
		sendStringMessage(mottakArkivPeLinux, message, CALL_ID);

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String received = receive(SVARKOSTRING);
			assertEquals(received, createReplyToKvittering(BREVSTATUS_FEIL, PENSJON_SYSTEM_ID, FORMAT, FEIL_BREV_EKSISTERER));
		});
	}

	@Test
	public void shouldFailOnBisysKvittering() {
		String message = createBisysKvittering();
		sendStringMessage(mottakArkivPeLinux, message, CALL_ID);

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String received = receive(deadletter);
			assertEquals(received, message);
		});
	}

	@Test
	public void shouldFailOnNullKvittering() {
		sendStringMessage(mottakArkivPeLinux, null, CALL_ID);

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			ActiveMQMessage received = receive(deadletter);
			Assertions.assertEquals(received.getJMSCorrelationID(), CORRELATION_ID);
		});
	}

	private String createReplyToKvittering(String status, String fagsystem, String format, String feilkode) {
		return """
				<?xml version="1.0" encoding="ISO-8859-1" ?>
				<rtv-brevkvitt>
				<brevref>%s</brevref>
				<sysid>%s</sysid>
				<type>%s</type>
				<status>%s</status>
				<feilkode>%s</feilkode>
				</rtv-brevkvitt>""".formatted(BREVREFERANSE, fagsystem, format, status, feilkode);
	}
}