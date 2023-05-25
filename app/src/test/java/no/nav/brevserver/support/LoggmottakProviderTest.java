package no.nav.brevserver.support;

import no.nav.brevserver.service.loggmottak.LoggmottakService;
import no.nav.brevserver.ws.loggmottak.map.LoggRequestMapper;
import no.nav.brevserver.ws.loggmottak.provider.LoggmottakProvider;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LoggmottakProvider
 */
@ExtendWith(MockitoExtension.class)
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
