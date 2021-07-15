package no.nav.brevserver.service.dokumentbehandling.support;

import no.nav.brevserver.service.brevlager.BrevlagerService;
import no.nav.brevserver.service.brevlager.BrevlagerServiceFactory;
import no.nav.brevserver.service.dokumentbehandling.PingService;

/**
 * Default implementation of PingService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultPingService implements PingService {

	@Override
	public void ping() {
		BrevlagerService service = BrevlagerServiceFactory.getInstance().createBrevlagerService();
		service.ping();
	}
}
