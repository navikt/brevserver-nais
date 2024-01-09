package no.nav.brevserver.ws.loggmottak;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.ws.loggmottak.provider.LoggmottakProvider;
import no.nav.tjenester.brevogarkiv.loggmottak.Logg;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggResponse;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggmottakPortType;
import no.nav.tjenester.brevogarkiv.loggmottak.ObjectFactory;
import org.slf4j.MDC;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import jakarta.xml.bind.JAXBElement;

@Slf4j
@Endpoint
public class LoggmottakEndpoint implements LoggmottakPortType {
	private static final String NAMESPACE_URI = "http://loggmottak.brevogarkiv.tjenester.nav.no/";
	private final LoggmottakProvider loggmottakProvider;
	private final ObjectFactory objectFactory;

	public LoggmottakEndpoint(LoggmottakProvider loggmottakProvider) {
		this.loggmottakProvider = loggmottakProvider;
		this.objectFactory = new ObjectFactory();
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "logg")
	@ResponsePayload
	public JAXBElement<LoggResponse> logg(@RequestPayload JAXBElement<Logg> logg) {
		try {
			logg(logg.getValue().getRequest());
			return objectFactory.createLoggResponse(new LoggResponse());
		} catch (Exception e) {
			log.error("Loggmottak - Klarte ikke logge request fra brevklient", e);
			return objectFactory.createLoggResponse(new LoggResponse());
		} finally {
			MDC.clear();
		}
	}

	@Override
	public void logg(LoggRequest loggRequest) {
		loggmottakProvider.logg(loggRequest);
	}
}
