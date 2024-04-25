package no.nav.brevserver.bestillBrev.biSys;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import config.AbstractTest;
import no.nav.brevserver.bestillBrev.Utils;
import no.nav.brevserver.core.vo.BrevStatusVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_BREVPAKKE;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BestillBrevRouteIT extends AbstractTest {

	private final String BREVREF_XML = "3835845842";

	@AfterEach
	public void cleanUp(){
		super.cleanupDb();
	}

	@Test
	public void shouldBestillNewBrev() throws Exception{
		String message = Utils.classpathToString("brevXml/bisysBrev.xml");
		sendStringMessage(onlinebrev, message, Utils.CALLID);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String recieved = receive(dialogueOnline);
			assertNotNull(recieved);
			BrevStatusVO endretBrevstatusVo  = brevstatusService.hentBrevStatus(BREVREF_XML, Utils.BISYS_SYSTEM_ID);
			assertEquals(BREVSTATUS_BREVPAKKE, endretBrevstatusVo.getStatus());
		});

	}

	@Test
	public void shouldGiTilgang() throws Exception{
		Logger loggen =(Logger) LoggerFactory.getLogger(BestillBrevRoute.class);
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		listAppender.start();
		loggen.addAppender(listAppender);
		String message = Utils.classpathToString("brevXml/fraBrevlager.xml");
		sendStringMessage(onlinebrev, message, Utils.CALLID);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			listAppender.list.contains("TIlgang gitt. Håndtering avsluttes");
			assertTrue(brevtilgangService.sjekkTilgang("BI12", "92fa00f8d8024b0", "klientToken"));
		});

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

}