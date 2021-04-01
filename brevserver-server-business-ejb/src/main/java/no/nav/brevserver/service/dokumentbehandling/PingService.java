package no.nav.brevserver.service.dokumentbehandling;

/**
 * Service that checks if lower levels can be contacted
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface PingService {
	/**
	 * Pings the lower levels
	 */
	void ping();
}
