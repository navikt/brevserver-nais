package no.nav.brevserver.ws.loggmottak.map.support;

import no.nav.brevserver.service.loggmottak.Log;
import no.nav.tjenester.brevogarkiv.loggmottak.Severity;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for SeverityToLogSeverityCustomConverter
 */
public class SeverityToLogSeverityCustomConverterTest {

	private SeverityToLogSeverityCustomConverter severityCustomConverter;

	@Before
	public void setUp() {
		severityCustomConverter = new SeverityToLogSeverityCustomConverter();
	}

	@Test
	public void shouldConvertFromSeverityToDownscaledLogSeverity() {
		convertAndAssertSeverityToLogSeverity(Severity.FATAL, Log.FATAL);
		convertAndAssertSeverityToLogSeverity(Severity.ERROR, Log.ERROR);
		convertAndAssertSeverityToLogSeverity(Severity.WARNING, Log.WARNING);
		convertAndAssertSeverityToLogSeverity(Severity.INFO, Log.INFO);
	}

	private void convertAndAssertSeverityToLogSeverity(Severity severity, int expectedLogSeverity) {
		int actualLogSeverity = severityCustomConverter.convertTo(severity);
		assertThat(actualLogSeverity, is(expectedLogSeverity));
	}
}
