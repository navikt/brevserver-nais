package no.nav.brevserver.provider.ws.loggmottak;

import no.nav.brevserver.provider.support.LoggmottakProvider;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for LoggmottakEndpoint
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class LoggmottakEndpointTest {

	@Mock
	private LoggmottakProvider loggmottakProviderMock;

	@InjectMocks
	private LoggmottakEndpoint loggmottakEndpoint;

	@Test
	public void shoulDelegateToProviderLogg() {
		LoggRequest loggRequest = new LoggRequest();

		loggmottakEndpoint.logg(loggRequest);

		verify(loggmottakProviderMock).logg(loggRequest);
	}
}
