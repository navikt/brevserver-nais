package no.nav.brevserver.service.queue.xml;


import no.nav.brevserver.service.queue.xml.beans.XMLServiceBean;

public enum XMLServiceFactory {

	INSTANCE;

	private XMLService xmlService;

	private XMLServiceFactory() {
		xmlService = new XMLServiceBean();
	}

	/**
	 * Convience method.
	 * 
	 * @return The INSTANCE of XMLServiceFactory
	 */
	public static XMLServiceFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Getter for {@link XMLService}.
	 * 
	 * @return An instance of {@link XMLService}.
	 */
	public XMLService createXMLService() {
		return xmlService;
	}
}
