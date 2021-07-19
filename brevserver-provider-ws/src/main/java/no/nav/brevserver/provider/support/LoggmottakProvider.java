package no.nav.brevserver.provider.support;

import no.nav.brevserver.provider.map.LoggRequestMapper;
import no.nav.brevserver.service.loggmottak.LoggmottakService;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggmottakPortType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provider that maps from and to the Loggmottak webservice model and delegates to Service layer implementations.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class LoggmottakProvider implements LoggmottakPortType {

	private final LoggmottakService loggmottakService;
	private final LoggRequestMapper loggRequestMapper;

	@Autowired
	public LoggmottakProvider(LoggmottakService loggmottakService, LoggRequestMapper loggRequestMapper) {
		this.loggmottakService = loggmottakService;
		this.loggRequestMapper = loggRequestMapper;
	}

	@Override
	public void logg(LoggRequest loggRequest) {
		loggmottakService.logg(loggRequestMapper.map(loggRequest));
	}
}
