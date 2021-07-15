package no.nav.brevserver.service.dokumentbehandling.support;

import no.nav.brevserver.service.brevlager.BrevlagerService;
import no.nav.brevserver.service.brevlager.BrevlagerServiceFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Unit tests for DefaultPingService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BrevlagerServiceFactory.class})
public class DefaultPingServiceTest {

	@Mock
	private BrevlagerService brevlagerServiceMock;

	private DefaultPingService pingService;

	@Before
	public void setUp() {
		setupBrevlagerService();
		pingService = new DefaultPingService();
	}

	@Test
	public void shouldCallBrevlagerPing() {
		pingService.ping();

		verify(brevlagerServiceMock).ping();
	}

	private void setupBrevlagerService() {
		mockStatic(BrevlagerServiceFactory.class);
		BrevlagerServiceFactory brevlagerServiceFactoryMock = mock(BrevlagerServiceFactory.class);
		when(BrevlagerServiceFactory.getInstance()).thenReturn(brevlagerServiceFactoryMock);
		when(brevlagerServiceFactoryMock.createBrevlagerService()).thenReturn(brevlagerServiceMock);
	}
}
