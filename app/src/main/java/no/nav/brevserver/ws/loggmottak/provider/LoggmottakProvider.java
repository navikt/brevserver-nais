package no.nav.brevserver.ws.loggmottak.provider;

import no.nav.brevserver.service.loggmottak.LoggmottakService;
import no.nav.brevserver.ws.loggmottak.map.LoggRequestMapper;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggmottakPortType;
import org.springframework.stereotype.Service;

/**
 * Provider that maps from and to the Loggmottak webservice model and delegates to Service layer implementations.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Service
public class LoggmottakProvider implements LoggmottakPortType {

	private final LoggmottakService loggmottakService;
	private final LoggRequestMapper loggRequestMapper;

	public LoggmottakProvider(LoggmottakService loggmottakService, LoggRequestMapper loggRequestMapper) {
		this.loggmottakService = loggmottakService;
		this.loggRequestMapper = loggRequestMapper;
	}

	@Override
	public void logg(LoggRequest loggRequest) {
		loggmottakService.logg(loggRequestMapper.map(loggRequest));
	}
}
