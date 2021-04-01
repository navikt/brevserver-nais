package no.nav.brevserver.service.loggmottak;

import no.nav.brevserver.service.loggmottak.to.LoggRequest;

/**
 * Service that does logging
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface LoggmottakService {
	/**
	 * Logs the contents of the request
	 *
	 * @param loggRequest The request object
	 */
	void logg(LoggRequest loggRequest);
}
