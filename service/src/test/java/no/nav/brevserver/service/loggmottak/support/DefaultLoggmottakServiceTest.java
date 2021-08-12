package no.nav.brevserver.service.loggmottak.support;

import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.service.H2JpaConfig;
import no.nav.brevserver.service.loggmottak.exception.LoggedException;
import no.nav.brevserver.service.loggmottak.to.LoggRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Unit test for DefaultLoggmottakService class
 *
 * @author Nabil Fario, Visma Consulting
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {H2JpaConfig.class})
public class DefaultLoggmottakServiceTest {

	private static final String SYSTEM_ID = "SYSTEM_ID";
	private static final String BREVREFERANSE = "BREVREFERANSE";
	private static final String INFOTRYGD_ID = "INFOTRYGD_ID";
	private static final String KLIENT_VERSION = "KLIENT_VERSION";
	private static final String BRUKER_ID = "BRUKER_ID";
	private static final String MESSAGE = "MESSAGE";
	private static final String EXCEPTION_MESSAGE = "EXCEPTION_MESSAGE";
	private static final String EXCEPTION_STACKTRACE = "EXCEPTION_STACKTRACE";

	private LoggRequest loggRequest;
	@Autowired
	private DefaultLoggmottakService defaultLoggmottakService;

	@Mock
	private Log logMock;

	@Captor
	private ArgumentCaptor<String> methSigCaptor;

	@Captor
	private ArgumentCaptor<String> messageCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		createLogRequest();
		defaultLoggmottakService.setLog(logMock);
	}

	@Test
	public void shouldLoggFatal() throws Exception {
		loggRequest.setSeverity(Log.FATAL);
		defaultLoggmottakService.logg(loggRequest);
		verify(logMock).fatal(methSigCaptor.capture(), messageCaptor.capture());
		assetMethSig(methSigCaptor.getValue());
		assertMessage(messageCaptor.getValue());
	}

	@Test
	public void shouldLoggError() throws Exception {
		loggRequest.setSeverity(Log.ERROR);
		defaultLoggmottakService.logg(loggRequest);
		verify(logMock).error(methSigCaptor.capture(), messageCaptor.capture());
		assetMethSig(methSigCaptor.getValue());
		assertMessage(messageCaptor.getValue());
	}

	@Test
	public void shouldLoggWarning() throws Exception {
		loggRequest.setSeverity(Log.WARNING);
		defaultLoggmottakService.logg(loggRequest);
		verify(logMock).warning(methSigCaptor.capture(), messageCaptor.capture());
		assetMethSig(methSigCaptor.getValue());
		assertMessage(messageCaptor.getValue());
	}

	@Test
	public void shouldLoggInfo() throws Exception {
		loggRequest.setSeverity(Log.INFO);
		defaultLoggmottakService.logg(loggRequest);
		verify(logMock).info(methSigCaptor.capture(), messageCaptor.capture());
		assetMethSig(methSigCaptor.getValue());
		assertMessage(messageCaptor.getValue());
	}

	@Test
	public void shouldErrorOnDebug() throws Exception {
		loggRequest.setSeverity(Log.DEBUG);
		defaultLoggmottakService.logg(loggRequest);
		verify(logMock).error(methSigCaptor.capture(), messageCaptor.capture());
		assetMethSig(methSigCaptor.getValue());
		assertThat(messageCaptor.getValue(), containsString(DefaultLoggmottakService.UNKNOWN_SEVERITY_MESSAGE));
		assertMessage(messageCaptor.getValue());
	}

	public void assetMethSig(String methSig) {
		assertThat(methSig, containsString(SYSTEM_ID));
		assertThat(methSig, containsString(BREVREFERANSE));
		assertThat(methSig, containsString(INFOTRYGD_ID));
		assertThat(methSig, containsString(KLIENT_VERSION));
		assertThat(methSig, containsString(BRUKER_ID));
	}

	public void assertMessage(String message) {
		assertThat(message, containsString(MESSAGE));
		assertThat(message, containsString(EXCEPTION_MESSAGE));
		assertThat(message, containsString(EXCEPTION_STACKTRACE));
	}

	private void createLogRequest() {
		loggRequest = new LoggRequest();
		loggRequest.setSystemId(SYSTEM_ID);
		loggRequest.setBrevreferanse(BREVREFERANSE);
		loggRequest.setInfotrygdId(INFOTRYGD_ID);
		loggRequest.setKlientVersion(KLIENT_VERSION);
		loggRequest.setBrukerId(BRUKER_ID);
		loggRequest.setMessage(MESSAGE);
		loggRequest.setException(new LoggedException(EXCEPTION_MESSAGE, EXCEPTION_STACKTRACE));
	}

}
