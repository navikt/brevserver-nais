/**
 * Beskrivelse av klassen
 *
 * @author Dag Kristiansen
 */
package no.nav.brevserver.server.common.type;

public final class QueueType {
    public static final QueueType PE_DIALOGUE_ONLINE = new QueueType("PE_DIALOGUE_ONLINE");
    public static final QueueType BI_DIALOGUE_ONLINE = new QueueType("BI_DIALOGUE_ONLINE");
    public static final QueueType BI_BREVSERVER_MOTTAK_ONLINE = new QueueType("BI_BREVSERVER_MOTTAK_ONLINE");
    public static final QueueType PE_BREVSERVER_MOTTAK_ONLINE = new QueueType("PE_BREVSERVER_MOTTAK_ONLINE");
    public static final QueueType PE_DIALOGUE_BATCH_RTF = new QueueType("PE_DIALOGUE_BATCH_RTF");
    public static final QueueType BI_DIALOGUE_BATCH_RTF = new QueueType("BI_DIALOGUE_BATCH_RTF");
    public static final QueueType BI_BREVSERVER_MOTTAK_ARKIV = new QueueType("BI_BREVSERVER_MOTTAK_ARKIV");
    public static final QueueType PE_BREVSERVER_MOTTAK_ARKIV = new QueueType("PE_BREVSERVER_MOTTAK_ARKIV");
    public static final QueueType PE_BREVSERVER_ONLINEBREV = new QueueType("PE_BREVSERVER_ONLINEBREV");
    public static final QueueType BI_BREVSERVER_ONLINEBREV = new QueueType("BI_BREVSERVER_ONLINEBREV");

    private final String myName; // for debug only

	private QueueType(String name) {
        myName = name;
    }

    public String toString() {
        return myName;
    }
}
