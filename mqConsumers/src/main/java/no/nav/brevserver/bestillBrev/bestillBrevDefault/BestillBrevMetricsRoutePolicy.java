package no.nav.brevserver.bestillBrev.bestillBrevDefault;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.ValidationException;
import org.apache.camel.support.RoutePolicySupport;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.brevserver.bestillBrev.bestillBrevDefault.BestillBrevRoute.BESTILLBREV;
import static no.nav.brevserver.core.metrics.MetricLabels.LABEL_ERROR_TYPE;
import static no.nav.brevserver.core.metrics.MetricLabels.LABEL_EXCEPTION_NAME;
import static no.nav.brevserver.core.metrics.MetricLabels.LABEL_PROCESS;
import static no.nav.brevserver.core.metrics.MetricLabels.TYPE_FUNCTIONAL_EXCEPTION;
import static no.nav.brevserver.core.metrics.MetricLabels.TYPE_TECHNICAL_EXCEPTION;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class BestillBrevMetricsRoutePolicy extends RoutePolicySupport {

	private final MeterRegistry registry;
	private Timer.Sample timer;

	static final String BESTILL_BREV_PROCESS_TIMER = "dok_request_latency";
	private static final String BESTILL_BREV_PROCESS_TIMER_DESCRIPTION = "prosesseringstid for kall inn til arkiverBrev";
	private static final String BESTILL_BREV_EXCEPTION = "dok_request_exception_total";

	@Inject
	public BestillBrevMetricsRoutePolicy(MeterRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void onExchangeBegin(Route route, Exchange exchange) {
		timer = Timer.start(registry);
	}

	@Override
	public void onExchangeDone(Route route, Exchange exchange) {
		Exception exception = getException(exchange);

		timer.stop(Timer.builder(BESTILL_BREV_PROCESS_TIMER)
				.description(BESTILL_BREV_PROCESS_TIMER_DESCRIPTION)
				.tags(LABEL_PROCESS, BESTILLBREV)
				.publishPercentileHistogram(true)
				.register(registry));

		if (exception != null) {
			if (isFunctionalException(exception)) {
				registry.counter(BESTILL_BREV_EXCEPTION,
						LABEL_ERROR_TYPE, TYPE_FUNCTIONAL_EXCEPTION,
						LABEL_EXCEPTION_NAME, exception.getClass().getSimpleName(),
						LABEL_PROCESS, BESTILLBREV).increment();
			} else {
				registry.counter(BESTILL_BREV_EXCEPTION,
						LABEL_ERROR_TYPE, TYPE_TECHNICAL_EXCEPTION,
						LABEL_EXCEPTION_NAME, exception.getClass().getCanonicalName(),
						LABEL_PROCESS, BESTILLBREV).increment();
			}
		}
	}

	private boolean isFunctionalException(Exception e) {
		return (e instanceof BrevFunctionalException || e instanceof ValidationException);
	}

	private Exception getException(Exchange exchange) {
		Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		if (exception == null && exchange.getException() instanceof Exception) {
			exception = (Exception) exchange.getException().getCause();
		}
		return exception;
	}
}
