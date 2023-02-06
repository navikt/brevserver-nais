package no.nav.brevserver.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Component
public class Metrics {

	private final String BREVKODE = "brevkode";
	private final String FAGSYSTEM = "fagsystem";

	private static MeterRegistry registry;

	public Metrics(MeterRegistry registry) {
		this.registry = registry;
	}


	public void incrementBrevkodeMetric(String fagsystem, String brevkode) {
		Counter.builder("dok_brevkode_opprettet_counter")
				.tag(FAGSYSTEM, determineFagsystem(fagsystem))
				.tags(BREVKODE, brevkode)
				.register(registry)
				.increment();
	}

	private String determineFagsystem(String fagsystem){
		return isEmpty(fagsystem) ? "UKJENT" : fagsystem;
	}
}
