package no.nav.brevserver.service.brevlager;

import no.nav.brevserver.service.brevlager.beans.BrevlagerServiceBean;

/**
 * Factory for obtaining an instance of {@link BrevlagerService}.
 * 
 * @author Marius Thøring, Visma Consulting
 */
public enum BrevlagerServiceFactory {

	INSTANCE;

	private BrevlagerService brevlagerService;

	private BrevlagerServiceFactory() {
		this.brevlagerService = new BrevlagerServiceBean();
	}

	/**
	 * Convience method.
	 * 
	 * @return The INSTANCE of {@link BrevlagerServiceFactory}.
	 */
	public static BrevlagerServiceFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Getter for {@link BrevlagerService}.
	 * 
	 * @return An instance of {@link BrevlagerService}.
	 */
	public BrevlagerService createBrevlagerService() {
		return this.brevlagerService;
	}
}
