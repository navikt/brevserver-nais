package no.nav.brevserver.provider.support;

import no.nav.brevserver.provider.map.LoggRequestMapper;
import no.nav.brevserver.provider.map.support.DefaultLoggRequestMapper;
import no.nav.brevserver.service.loggmottak.LoggmottakService;
import no.nav.brevserver.service.loggmottak.support.DefaultLoggmottakService;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest;
import no.nav.tjenester.brevogarkiv.loggmottak.LoggmottakPortType;

/**
 * Provider that maps from and to the Loggmottak webservice model and delegates to Service layer implementations.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class LoggmottakProvider implements LoggmottakPortType {

	private LoggmottakService loggmottakService;
	private LoggRequestMapper loggRequestMapper;

	public LoggmottakProvider() {
		loggmottakService = new DefaultLoggmottakService();
		loggRequestMapper = new DefaultLoggRequestMapper();
	}

	@Override
	public void logg(LoggRequest loggRequest) {
		loggmottakService.logg(loggRequestMapper.map(loggRequest));
	}
}
