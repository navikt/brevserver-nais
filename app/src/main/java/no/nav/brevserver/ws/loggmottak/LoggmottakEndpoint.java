package no.nav.brevserver.ws.loggmottak;

import no.nav.brevserver.ws.loggmottak.provider.LoggmottakProvider;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggmottakPortType;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;


/**
 * Implementation of the JAX-WS generated service interface LoggmottakPortType.
 * Delegates to LoggmottakProvider at the provider layer.
 */
@Endpoint
public class LoggmottakEndpoint implements LoggmottakPortType {

	private final LoggmottakProvider loggmottakProvider;
	private static final String NAMESPACE_URI = "http://loggmottak.brevogarkiv.tjenester.nav.no/";

	public LoggmottakEndpoint(LoggmottakProvider loggmottakProvider) {
		this.loggmottakProvider = loggmottakProvider;
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "logg")
	@ResponsePayload
	public void logg(@RequestPayload LoggRequest request) {
		loggmottakProvider.logg(request);
	}
}
