package no.nav.brevserver.provider.ws.loggmottak;

import no.nav.brevserver.provider.support.LoggmottakProvider;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggmottakPortType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jws.WebService;

/**
 * Implementation of the JAX-WS generated service interface LoggmottakPortType.
 * Delegates to LoggmottakProvider at the provider layer.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@WebService(endpointInterface = "no.nav.tjenester.brevogarkiv.loggmottak.LoggmottakPortType",
		serviceName = "Loggmottak",
		portName = "LoggmottakPort")
public class LoggmottakEndpoint implements LoggmottakPortType {

	private final LoggmottakProvider loggmottakProvider;

	@Autowired
	public LoggmottakEndpoint(LoggmottakProvider loggmottakProvider) {
		this.loggmottakProvider = loggmottakProvider;
	}

	@Override
	public void logg(LoggRequest loggRequest) {
		loggmottakProvider.logg(loggRequest);
	}
}
