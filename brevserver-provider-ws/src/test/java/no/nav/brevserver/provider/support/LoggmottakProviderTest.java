package no.nav.brevserver.provider.support;

import no.nav.brevserver.provider.map.LoggRequestMapper;
import no.nav.brevserver.service.loggmottak.LoggmottakService;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LoggmottakProvider
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class LoggmottakProviderTest {

	@Mock
	private LoggmottakService loggmottakServiceMock;
	@Mock
	private LoggRequestMapper loggRequestMapperMock;

	@InjectMocks
	private LoggmottakProvider loggmottakProvider;

	@Test
	public void shouldDelegateLoggToLoggmottakService() throws Exception {
		LoggRequest wsRequest = new LoggRequest();
		no.nav.brevserver.service.loggmottak.to.LoggRequest domainRequest =
				new no.nav.brevserver.service.loggmottak.to.LoggRequest();

		when(loggRequestMapperMock.map(wsRequest)).thenReturn(domainRequest);

		loggmottakProvider.logg(wsRequest);

		verify(loggmottakServiceMock).logg(domainRequest);
	}
}
