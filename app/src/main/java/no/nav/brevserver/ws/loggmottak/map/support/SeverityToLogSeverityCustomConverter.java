package no.nav.brevserver.ws.loggmottak.map.support;

import no.nav.brevserver.service.loggmottak.Log;
import no.nav.tjenester.brevogarkiv.loggmottak.Severity;

/**
 * Maps from webservice Severity to a down-scaled Log.severity.
 */
public class SeverityToLogSeverityCustomConverter {

	public Integer convertTo(Severity source) {
		return switch (source) {
			case FATAL -> Log.FATAL;
			case ERROR -> Log.ERROR;
			case WARNING -> Log.WARNING;
			case INFO -> Log.INFO;
		};
	}
}
