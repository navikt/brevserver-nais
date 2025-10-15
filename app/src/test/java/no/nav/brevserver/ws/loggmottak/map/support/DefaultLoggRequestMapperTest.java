package no.nav.brevserver.ws.loggmottak.map.support;

import no.nav.tjenester.brevogarkiv.loggmottak.BrevklientArguments;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest;
import no.nav.tjenester.brevogarkiv.loggmottak.Severity;
import no.nav.tjenester.brevogarkiv.loggmottak.WrappedException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;

import static no.nav.brevserver.service.loggmottak.Log.ERROR;
import static org.assertj.core.api.Assertions.assertThat;


public class DefaultLoggRequestMapperTest {

	private static final String SYSTEM_ID = "PE2";
	private static final String BREVREFERANSE = "123";
	private static final String INFOTRYGD_ID = "456";
	private static final String BRUKER_ID = "Donald!";
	private static final String BREVKLIENT_VERSION = "v9.0.0";

	private static final String LOGMESSAGE = "Dette er en beskjed";
	private static final String EXCEPTION_MESSAGE = "test";
	private String stacktrace;

	private final DefaultLoggRequestMapper loggRequestMapper = new DefaultLoggRequestMapper();

	@Test
	public void shouldMapFromWsRequestToDomainRequest() {
		no.nav.brevserver.service.loggmottak.to.LoggRequest domainRequest = loggRequestMapper.map(createWsLoggRequest());

		assertThat(domainRequest.getSystemId()).isEqualTo(SYSTEM_ID);
		assertThat(domainRequest.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(domainRequest.getInfotrygdId()).isEqualTo(INFOTRYGD_ID);
		assertThat(domainRequest.getKlientVersion()).isEqualTo(BREVKLIENT_VERSION);
		assertThat(domainRequest.getBrukerId()).isEqualTo(BRUKER_ID);
		assertThat(domainRequest.getSeverity()).isEqualTo(ERROR);
		assertThat(domainRequest.getMessage()).isEqualTo(LOGMESSAGE);

		assertThat(domainRequest.getException().getMessage()).isEqualTo(EXCEPTION_MESSAGE);
		assertThat(domainRequest.getException().getStacktrace()).isEqualTo(stacktrace);
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