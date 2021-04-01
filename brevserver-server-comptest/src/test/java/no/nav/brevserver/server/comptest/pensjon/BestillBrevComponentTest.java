package no.nav.brevserver.server.comptest.pensjon;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import no.nav.brevserver.server.comptest.AbstractComponentTest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Brevbestilling test.
 * <p>
 * Requirements:
 * <li>WAS listener for specified input queue must be started.</li>
 * 
 * @author Stian Landsnes, Visma Sirius
 *
 */
public class BestillBrevComponentTest extends AbstractComponentTest {

	private final String inputQueue = "queue:///D475.BREVSERVER_ONLINEBREV_PE01";
	private final String kvitteringQueue = "queue:///D475.BREVSERVER_DLQ_PE";
	
	private final String emptyMessage = "";
	
	@Before
	public void before() {
		cleanQueues(inputQueue, kvitteringQueue);
	}
	
	@Test
	@Ignore
	public void shouldSendFeilmeldingToFagsystemIfXmlError() throws Exception {
		messageHandler.sendTextMessage(inputQueue, emptyMessage);

		// Message should NOT be consumed from input queue by brevserver
		assertThat(messageHandler.consumeMessages(inputQueue, 1).size(), is(1));
		
		// Brevserver should send feilmelding back to fagsystem
		assertThat(messageHandler.consumeMessages(kvitteringQueue, 1).size(), is(1));
	}
	
}
