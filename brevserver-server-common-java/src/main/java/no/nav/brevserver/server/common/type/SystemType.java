/**
 * Beskrivelse av klassen
 *
 * @author Dag Kristiansen
 */
package no.nav.brevserver.server.common.type;

public final class SystemType {
    public static final SystemType BI = new SystemType("BI");
    public static final SystemType PE = new SystemType("PE");

    private final String myName; // for debug only

    private SystemType(String name) {
        myName = name;
    }

    public String toString() {
        return myName;
    }
}
