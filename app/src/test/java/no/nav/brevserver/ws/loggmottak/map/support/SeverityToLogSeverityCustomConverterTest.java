package no.nav.brevserver.ws.loggmottak.map.support;

import no.nav.brevserver.service.loggmottak.Log;
import no.nav.tjenester.brevogarkiv.loggmottak.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SeverityToLogSeverityCustomConverterTest {

	private final SeverityToLogSeverityCustomConverter severityCustomConverter = new SeverityToLogSeverityCustomConverter();

	@Test
	public void shouldConvertFromSeverityToDownscaledLogSeverity() {
		convertAndAssertSeverityToLogSeverity(Severity.FATAL, Log.FATAL);
		convertAndAssertSeverityToLogSeverity(Severity.ERROR, Log.ERROR);
		convertAndAssertSeverityToLogSeverity(Severity.WARNING, Log.WARNING);
		convertAndAssertSeverityToLogSeverity(Severity.INFO, Log.INFO);
	}

	private void convertAndAssertSeverityToLogSeverity(Severity severity, int expectedLogSeverity) {
		int actualLogSeverity = severityCustomConverter.convertTo(severity);

		assertThat(expectedLogSeverity).isEqualTo(actualLogSeverity);
	}

}