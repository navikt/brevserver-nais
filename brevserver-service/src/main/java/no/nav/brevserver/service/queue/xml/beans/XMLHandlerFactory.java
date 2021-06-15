package no.nav.brevserver.service.queue.xml.beans;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;

/**
 * Beskrivelse av klassen
 *
 * @author Dag Kristiansen
 */
public final class XMLHandlerFactory {
    private static XMLHandlerFactory ourInstance = new XMLHandlerFactory();

    public static XMLHandlerFactory getInstance() {
        return ourInstance;
    }

    private XMLHandlerFactory() {
    }

    /**
     * Creates command based on {@link javax.jms.Message}
     *
     * @return A XML Handler that parses messages for the given system type.
     */
    public XMLHandler createXmlHandler() throws BrevTechnicalException {
        return new XMLHandler();
    }
}
