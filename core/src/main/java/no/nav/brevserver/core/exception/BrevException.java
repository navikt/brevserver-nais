package no.nav.brevserver.core.exception;

/**
 * Exception som kastes ved funksjonelle feil
 */
public class BrevException extends Exception {
	private static final long serialVersionUID = 5744494456562063708L;

	public static final int UKJENT_FEIL = 1000;

	protected int feilkode = UKJENT_FEIL;

	public BrevException(String s) {
		super(s);
	}

	public BrevException(Exception e) {
		super(e);
	}

	public BrevException(int i, Exception e) {
		super(getFeilmelding(i), e);
		feilkode = i;
	}

	public BrevException(String s, Exception e) {
		super(s, e);
	}

	protected static String getFeilmelding(int i) {
		return "Ukjent feil";
	}

	public boolean isFeilkode(int i) {
		if (i == feilkode) {
			return true;
		}
		return false;
	}

}