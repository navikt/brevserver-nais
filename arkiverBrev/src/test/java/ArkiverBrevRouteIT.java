import jakarta.jms.Message;
import jakarta.jms.Queue;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import utils.Utils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static utils.Utils.BISYS_SYSTEM_ID;
import static utils.Utils.BREVREFERANSE2;
import static utils.Utils.CALLID;
import static utils.Utils.STATUS_FERDIG;
import static utils.Utils.classpathToString;
import static utils.Utils.createBisysKvittering2;
import static utils.Utils.createBrevstatus;


public class ArkiverBrevRouteIT extends AbstractTest {

	@Autowired
	protected Queue mottakArkiv;
	@Autowired
	protected Queue deadletter;

	private static final String CORRELATION_ID = "1890432+12342341";
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

	protected void lagreDefaultBrevStatusVo() throws BrevTechnicalException {
		BrevStatusVO brevstatus = createBrevstatus(BISYS_SYSTEM_ID, Utils.BREVREFERANSE);
		brevstatusService.lagreBrevStatus(brevstatus);
	}
}