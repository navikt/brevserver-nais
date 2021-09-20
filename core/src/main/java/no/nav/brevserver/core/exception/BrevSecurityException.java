package no.nav.brevserver.core.exception;

/**
 * Exception som kastes ved sikkerhetproblemer
 * 
 * @author Rune Røren, Accenture
 * @version $Revision: 1989 $ $Author: t133126 $ $Date: 2012-06-14 09:51:55 +0200 (to, 14 jun 2012) $
 */

public class BrevSecurityException extends BrevException {
	private static final long serialVersionUID = -5266156350680012828L;
	public static final int IKKE_TILGANG_I_BREVSERVER = 3001;

	public BrevSecurityException(int i) {
		super(getFeilmelding(i));
		
		this.feilkode = i;
	}

	public BrevSecurityException(String s) {
		super(s);
	}

	public BrevSecurityException(String s, int i) {
		super(s);
		
		this.feilkode = i;
	}
	
	public BrevSecurityException(String s, int i, Exception e) {
		super(s, e);
		
		this.feilkode = i;		
	}
	
	public BrevSecurityException(int i, Exception e) {
		super(getFeilmelding(i), e);
		
		this.feilkode = i;		
	}	
	
	protected static String getFeilmelding(int i) {
		String result = null;
		
		switch(i) {
			case IKKE_TILGANG_I_BREVSERVER : 
				result = "Ikke tilgang i Brevserver";
				break;
			default : 
				BrevException.getFeilmelding(i);
		}
		
		return result;
	}
	

}