package no.nav.brevserver.arkiverBrev;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.ValidationException;
import org.apache.camel.support.RoutePolicySupport;
import org.springframework.stereotype.Component;

import static no.nav.brevserver.arkiverBrev.BiSys.ArkiverBrevRoute.ARKIVER_BREV_ROUTE;
import static no.nav.brevserver.core.metrics.MetricLabels.LABEL_ERROR_TYPE;
import static no.nav.brevserver.core.metrics.MetricLabels.LABEL_EXCEPTION_NAME;
import static no.nav.brevserver.core.metrics.MetricLabels.LABEL_PROCESS;
import static no.nav.brevserver.core.metrics.MetricLabels.TYPE_FUNCTIONAL_EXCEPTION;
import static no.nav.brevserver.core.metrics.MetricLabels.TYPE_TECHNICAL_EXCEPTION;

@Component
public class ArkiverBrevMetricsRoutePolicy extends RoutePolicySupport {

	private final MeterRegistry registry;
	private static final String ARKIVER_BREV_EXCEPTION = "dok_request_exception_total";

	public ArkiverBrevMetricsRoutePolicy(MeterRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void onExchangeDone(Route route, Exchange exchange) {
		Exception exception = getException(exchange);

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
