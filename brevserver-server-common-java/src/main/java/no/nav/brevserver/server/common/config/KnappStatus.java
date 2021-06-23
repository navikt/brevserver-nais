package no.nav.brevserver.server.common.config;

/**
 * Denne klassen skal holde orden på hvilke knapper som skal enables / disables.
 * 
 * @author Rune Røren, Accenture
 * @version $Revision: 501 $ $Author: rra2920 $ $Date: 2005-08-30 14:54:19 +0200 (ti, 30 aug 2005) $
 */
public class KnappStatus {
	// Disse tallene m� tolkes bin�rt for � forst�s
	public static final int SKRIV_UT_KLADD = 0b0001;
	public static final int LAGRE_KLADD = 0b0010;
	public static final int FERDIGSTILL_LOKAL_UTSKRIFT = 0b0100;
	public static final int FERDIGSTILL = 0b1000;

	private int knappStatus = 0;

	public static int getDefaultValue() {
		return SKRIV_UT_KLADD + LAGRE_KLADD + FERDIGSTILL_LOKAL_UTSKRIFT;
	}

	public static int getAllActiveValue() {
		return SKRIV_UT_KLADD + LAGRE_KLADD + FERDIGSTILL_LOKAL_UTSKRIFT + FERDIGSTILL;
	}

	public static KnappStatus getDefault() {
		return new KnappStatus(getDefaultValue());
	}

	public KnappStatus(int value) {
		knappStatus = value;
	}

	public boolean isEnabled(int value) {
		return (knappStatus & value) > 0;
	}

	public String toBinaryString() {
		return Integer.toBinaryString(knappStatus);
	}

	public String toString() {
		return Integer.toString(knappStatus);
	}
}
