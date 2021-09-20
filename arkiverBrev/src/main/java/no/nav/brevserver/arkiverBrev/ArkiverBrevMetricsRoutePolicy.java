package no.nav.brevserver.arkiverBrev;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.ValidationException;
import org.apache.camel.support.RoutePolicySupport;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.brevserver.arkiverBrev.ArkiverBrevDefault.ArkiverBrevRoute.ARKIVER_BREV_ROUTE;
import static no.nav.brevserver.arkiverBrev.ArkiverBrevRoute.ARKIVER_BREV_ROUTE;
import static no.nav.brevserver.core.metrics.MetricLabels.LABEL_ERROR_TYPE;
import static no.nav.brevserver.core.metrics.MetricLabels.LABEL_EXCEPTION_NAME;
import static no.nav.brevserver.core.metrics.MetricLabels.LABEL_PROCESS;
import static no.nav.brevserver.core.metrics.MetricLabels.TYPE_FUNCTIONAL_EXCEPTION;
import static no.nav.brevserver.core.metrics.MetricLabels.TYPE_TECHNICAL_EXCEPTION;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class ArkiverBrevMetricsRoutePolicy extends RoutePolicySupport {

	private final MeterRegistry registry;
	private Timer.Sample timer;

	static final String ARKIVER_BREV_PROCESS_TIMER = "dok_request_latency";
	private static final String ARKIVER_BREV_PROCESS_TIMER_DESCRIPTION = "prosesseringstid for kall inn til arkiverBrev";
	private static final String ARKIVER_BREV_EXCEPTION = "dok_request_exception_total";

	@Inject
	public ArkiverBrevMetricsRoutePolicy(MeterRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void onExchangeBegin(Route route, Exchange exchange) {
		timer = Timer.start(registry);
	}

	@Override
	public void onExchangeDone(Route route, Exchange exchange) {
		Exception exception = getException(exchange);

		timer.stop(Timer.builder(ARKIVER_BREV_PROCESS_TIMER)
				.description(ARKIVER_BREV_PROCESS_TIMER_DESCRIPTION)
				.tags(LABEL_PROCESS, ARKIVER_BREV_ROUTE)
				.publishPercentileHistogram(true)
				.register(registry));

		if (exception != null) {
			if (isFunctionalException(exception)) {
				registry.counter(ARKIVER_BREV_EXCEPTION,
						LABEL_ERROR_TYPE, TYPE_FUNCTIONAL_EXCEPTION,
						LABEL_EXCEPTION_NAME, exception.getClass().getSimpleName(),
						LABEL_PROCESS, ARKIVER_BREV_ROUTE).increment();
			} else {
				registry.counter(ARKIVER_BREV_EXCEPTION,
						LABEL_ERROR_TYPE, TYPE_TECHNICAL_EXCEPTION,
						LABEL_EXCEPTION_NAME, exception.getClass().getCanonicalName(),
						LABEL_PROCESS, ARKIVER_BREV_ROUTE).increment();
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
