package no.nav.brevserver.core.exception;

/**
 * Exception som kastes ved tekniske feil
 * 
 * @author Rune Røren, Accenture
 * @version $Revision: 1991 $ $Author: t133126 $ $Date: 2012-06-14 11:20:26 +0200 (to, 14 jun 2012) $
 */
public class BrevTechnicalException extends BrevException {
	private static final long serialVersionUID = -4481452292318987654L;
	public static final int DATABASE_IKKE_TILGJENGELIG = 2000;
	public static final int MQ_IKKE_TILGJENGELIG = 2003;
	public static final int FEIL_I_XML = 2004;
	public static final int JNDI_OPPSLAG_FEILET = 2005;
	public static final int UGYLDIG_JOURNALSTATUS = 2007;

	public BrevTechnicalException(String s) {
		super(s);
	}

	public BrevTechnicalException(Exception e) {
		super(null, e);
	}

	public BrevTechnicalException(String s, Exception e) {
		super(s, e);
	}

	public BrevTechnicalException(int i, Exception e) {
		super(getFeilmelding(i), e);
		feilkode = i;
	}

	public BrevTechnicalException(int i, String msg) {
		super(msg);
		feilkode = i;
	}
	
	public BrevTechnicalException(int i, String msg, Exception e) {
		super(msg, e);
		feilkode = i;
	}

	protected static String getFeilmelding(int i) {
		String result = null;

		switch (i) {
		case DATABASE_IKKE_TILGJENGELIG:
			result = "Databasen til brevserveren er ikke tilgjengelig";
			break;

		case MQ_IKKE_TILGJENGELIG:
			result = "MQ er ikke tilgjengelig";
			break;

		case FEIL_I_XML:
			result = "Det er en feil i mottatt XML";
			break;

		case JNDI_OPPSLAG_FEILET:
			result = "ControllerBean er ikke tilgjengelig";
			break;

		case UGYLDIG_JOURNALSTATUS:
			result = "Ugyldig journalstatus i Joark";
			break;

		default:
			result = BrevException.getFeilmelding(i);
		}

		return result;
	}

}
