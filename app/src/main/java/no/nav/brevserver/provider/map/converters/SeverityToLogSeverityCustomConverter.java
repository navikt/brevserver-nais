package no.nav.brevserver.provider.map.converters;

import no.nav.brevserver.service.loggmottak.Log;
import no.nav.tjenester.brevogarkiv.loggmottak.Severity;
import org.dozer.DozerConverter;

/**
 * Maps from webservice Severity to a down-scaled Log.severity.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class SeverityToLogSeverityCustomConverter extends DozerConverter<Severity, Integer> {

	public SeverityToLogSeverityCustomConverter() {
		super(Severity.class, Integer.class);
	}

	@Override
	public Integer convertTo(Severity source, Integer destination) {
		switch (source) {
			case FATAL:
				return Log.FATAL;
			case ERROR:
				return Log.ERROR;
			case WARNING:
				return Log.WARNING;
			case INFO:
				return Log.INFO;
			default:
				throw new IllegalArgumentException(source.name() + " is not a valid severity");
		}
	}

	@Override
	public Severity convertFrom(Integer source, Severity destination) {
		throw new UnsupportedOperationException("Conversion from Log severity to Severity is not supported");
	}
}
