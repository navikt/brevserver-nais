package no.nav.brevserver.core.exception;

/**
 * Exception som kastes ved funksjonelle feil
 */
public class BrevFunctionalException extends BrevException {
	private static final long serialVersionUID = 7673864781940107640L;
	public static final int FANT_IKKE_DOKUMENT = 5000;
	public static final int DOKUMENTET_ER_RESERVERT = 50001;
	public static final int DOKUMENTET_ER_FERDIGSTILT = 50002;
	public static final int DOKUMENTET_ER_FLAGGET_FOR_KASSASJON = 50003;

	public BrevFunctionalException(int i, String msg) {
		super(msg);
		
		feilkode = i;
	}

	public BrevFunctionalException(int i, String msg, Exception e) {
		super(msg, e);
		
		feilkode = i;
	}

	public BrevFunctionalException(String s) {
		super(s);
	}
	
	public BrevFunctionalException(String s, Exception e) {
		super(s, e);
	}
}