package no.nav.brevserver.core.constants;

/**
 * Klasse som inneholder felles konstanter for Brevløsningen.
 */
public final class Konstanter {

	private Konstanter() {
		// noop
	}

	// konstanter for brevlageret
	public static final String BREVLAGER_STATUS_FERDIG = "FERDIG";
	public static final String BREVLAGER_STATUS_KLADD = "KLADD";

	// konstanter for brevstatus
	public static final String BREVSTATUS_BREVPAKKE = "BREVPAKK";
	public static final String BREVSTATUS_LAGRET_KLADD = "LAGRET";
	public static final String BREVSTATUS_FERDIG = "FERDIG";
	public static final String BREVSTATUS_UTSKRIFT = "UTSKRIFT";
	public static final String BREVSTATUS_FEIL = "FEIL";
	public static final String BREVSTATUS_AVBRUTT = "AVBRUTT";

	// Konstanter for ContentType
	public static final String CONTENTTYPE_DOCX_SHORT = "application/msword.docx";

	// Konstant for meldingene
	public static final int MELDING_HEADER_LENGTH = 350;

	public static final String FEIL_UKJENT = "90000001 Ukjent feil";
	public static final String FEIL_BREV_EKSISTERER = "90000003 Brevet eksisterer allerede";

	// Koder for Brevstatus Modus
	public static final String BREVMODUS_FRALAGER = "frabrevlager";

	public static final String SKRIVERTYPE_INGEN = "ingen";

	// Feilnivåer fra brevpakken
	public static final String BREVPAKKE_FEILNIVA_FEIL = "8";

	// Standard tegnsett
	public static final String TEGNSETT_ISO = "ISO-8859-1";
}