/**
 * Beskrivelse av klassen
 */
package no.nav.brevserver.core.constants;

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
