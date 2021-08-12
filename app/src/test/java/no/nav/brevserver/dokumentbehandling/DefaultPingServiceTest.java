package no.nav.brevserver.dokumentbehandling;

import no.nav.brevserver.nais.DokumentbehandlingProvider;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for DefaultPingService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(SpringRunner.class)
public class DefaultPingServiceTest {

	@Mock
	private BrevlagerService brevlagerServiceMock;

	@InjectMocks
	private DokumentbehandlingProvider dokumentbehandlingProvider;

	@Test
	public void shouldCallBrevlagerPing() {
		dokumentbehandlingProvider.ping(new PingRequest());
		verify(brevlagerServiceMock).ping();
	}


}
