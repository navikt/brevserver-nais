package no.nav.brevserver.provider.map.converters;

import no.nav.brevserver.server.common.log.Log;
import no.nav.tjenester.brevogarkiv.loggmottak.Severity;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for SeverityToLogSeverityCustomConverter
 *
 * @author Joakim Bjørnstad, Visma Consulting
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
		int actualLogSeverity = severityCustomConverter.convertTo(severity, null);
		assertThat(actualLogSeverity, is(expectedLogSeverity));
	}
}
