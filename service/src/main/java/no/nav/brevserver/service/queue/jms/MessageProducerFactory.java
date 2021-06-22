package no.nav.brevserver.service.queue.jms;

import no.nav.brevserver.server.common.type.SystemType;

/**
 * Beskrivelse av klassen
 *
 * @author Dag Kristiansen
 */
public final class MessageProducerFactory {
    private static MessageProducerFactory ourInstance = new MessageProducerFactory();

    public static MessageProducerFactory getInstance() {
        return ourInstance;
    }

    private MessageProducerFactory() {
    }

    /**
     * Creates command based on {@link javax.jms.Message}
     *
     * @param systemType
     * @return A MessageProducer that produces messages for the given system type.
     */
    public MessageProducer createMessageProducer(SystemType systemType) {
        if (systemType.equals(SystemType.BI)) {
            return new BIMessageProducer();
        } else if (systemType.equals(SystemType.PE)) {
            return new PEMessageProducer();
        } else {
            throw new RuntimeException("No message producer found for the given SystemType '" + systemType.toString() + "'");
        }
    }
}
