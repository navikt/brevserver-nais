package no.nav.brevserver.service.brevserver;

import no.nav.brevserver.service.brevserver.beans.BrevserverServiceBean;

/**
 * Factory for å lage BrevserverService klasser.
 */
public enum BrevserverServiceFactory {

	INSTANCE;

	private BrevserverService service;

	private BrevserverServiceFactory() {
		service = new BrevserverServiceBean();
	}

	/**
	 * Convience method.
	 *
	 * @return The INSTANCE of BrevserverServiceFactory
	 */
	public static BrevserverServiceFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Getter for {@link BrevserverService}.
	 *
	 * @return An instance of {@link BrevserverService}.
	 */
	public BrevserverService createBrevserverService() {
		return service;
	}
}
