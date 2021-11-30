package no.nav.brevserver.support;

import no.nav.brevserver.service.loggmottak.Log;
import no.nav.brevserver.service.loggmottak.exception.LoggedException;
import no.nav.brevserver.ws.loggmottak.map.support.DefaultLoggRequestMapper;
import no.nav.tjenester.brevogarkiv.loggmottak.BrevklientArguments;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest;
import no.nav.tjenester.brevogarkiv.loggmottak.Severity;
import no.nav.tjenester.brevogarkiv.loggmottak.WrappedException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for DefaultLoggRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultLoggRequestMapperTest {
	private static final String SYSTEM_ID = "PE2";
	private static final String BREVREFERANSE = "123";
	private static final String INFOTRYGD_ID = "456";
	private static final String BRUKER_ID = "Donald!";
	private static final String BREVKLIENT_VERSION = "v9.0.0";

	private static final String LOGMESSAGE = "Dette er en beskjed";
	private static final String EXCEPTION_MESSAGE = "test";
	private String stacktrace;

	private DefaultLoggRequestMapper loggRequestMapper;
	private LoggRequest wsRequest;
	private no.nav.brevserver.service.loggmottak.to.LoggRequest domainRequest;

	@Before
	public void setUp() {
		loggRequestMapper = new DefaultLoggRequestMapper();
		wsRequest = createWsLoggRequest();
	}

	@Test
	public void shouldMapFromWsRequestToDomainRequest() {
		domainRequest = loggRequestMapper.map(wsRequest);

		assertThat(domainRequest.getSystemId(), is(SYSTEM_ID));
		assertThat(domainRequest.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(domainRequest.getInfotrygdId(), is(INFOTRYGD_ID));
		assertThat(domainRequest.getKlientVersion(), is(BREVKLIENT_VERSION));
		assertThat(domainRequest.getBrukerId(), is(BRUKER_ID));
		assertThat(domainRequest.getSeverity(), is(Log.ERROR));
		assertThat(domainRequest.getMessage(), is(LOGMESSAGE));
		assertLoggedException(domainRequest.getException());
	}

	private void assertLoggedException(LoggedException loggedException) {
		assertThat(loggedException.getMessage(), is(EXCEPTION_MESSAGE));
		assertThat(loggedException.getStacktrace(), is(stacktrace));
	}

	private LoggRequest createWsLoggRequest() {
		LoggRequest loggRequest = new LoggRequest();
		loggRequest.setKlientVersion(BREVKLIENT_VERSION);
		loggRequest.setBrukerId(BRUKER_ID);
		loggRequest.setBrevklientArguments(createBrevklientArguments());
		loggRequest.setSeverity(Severity.ERROR);
		loggRequest.setMessage(LOGMESSAGE);
		loggRequest.setException(createWrappedException());
		return loggRequest;
	}

	private BrevklientArguments createBrevklientArguments() {
		BrevklientArguments brevklientArguments = new BrevklientArguments();
		brevklientArguments.setBrevreferanse(BREVREFERANSE);
		brevklientArguments.setSystemId(SYSTEM_ID);
		brevklientArguments.setInfotrygdId(INFOTRYGD_ID);
		return brevklientArguments;
	}

	private WrappedException createWrappedException() {
		WrappedException exception = new WrappedException();
		try {
			throw new IllegalAccessException(EXCEPTION_MESSAGE);
		} catch (IllegalAccessException e) {
			stacktrace = ExceptionUtils.getStackTrace(e);
		}
		exception.setMessage(EXCEPTION_MESSAGE);
		exception.setStacktrace(stacktrace);
		return exception;
	}
}
