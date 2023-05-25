package no.nav.brevserver.dokumentbehandling;

import no.nav.brevserver.nais.DokumentbehandlingProvider;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for DefaultPingService
 */
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
