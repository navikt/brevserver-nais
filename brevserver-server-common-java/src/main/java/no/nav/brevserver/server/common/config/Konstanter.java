package no.nav.brevserver.server.common.config;

/**
 * Klasse som inneholder felles konstanter for Brevløsningen.
 *
 * @author Holger Zobel, Accenture
 */
public final class Konstanter {

	private Konstanter() {

	}

	public static final String MASKED_PASSWORD = "*****";

	public static final String BREVLAGER_TABELL = "T_BREVLAGER5";
	public static final String BREVLAGER_HISTORIKK_TABELL = "T_BREVLAGER_HISTORIKK";

	// konstanter for brevlageret
	public static final String BREVLAGER_STATUS_FERDIG = "FERDIG";
	public static final String BREVLAGER_STATUS_KLADD = "KLADD";

	// konstanter for brevstatus
	public static final String BREVSTATUS_BREVPAKKE = "BREVPAKK";
	public static final String BREVSTATUS_HOS_BREVKLIENT = "KLIENT";
	public static final String BREVSTATUS_LAGRET_KLADD = "LAGRET";
	public static final String BREVSTATUS_FERDIG = "FERDIG";
	public static final String BREVSTATUS_UTSKRIFT = "UTSKRIFT";
	public static final String BREVSTATUS_FEIL = "FEIL";
	public static final String BREVSTATUS_AVBRUTT = "AVBRUTT";

	// Konstanter for ContentType
	public static final String CONTENTTYPE_DOCX_SHORT = "application/msword.docx";

	// Konstant for meldingene
	public static final int MELDING_HEADER_LENGTH = 350;

	// Konstant for meldingsflytservices for BiSys
	public static final String KONF_SEND_DIALOGUE_ONLINE_BI = "jms/queue/dialogueOnline";
	public static final String KONF_MOTTAK_DIALOGUE_ONLINE_BI = "jms/queue/brevserverMottakOnline";
	public static final String KONF_MOTTAK_DIALOGUE_ARKIV_BI = "jms/queue/brevserverMottakArkiv";
	public static final String KONF_MOTTAK_SAKSBEH_ONLINE_BI = "jms/queue/brevserverOnlinebrev";
	public static final String KONF_DEAD_LETTER_BI = "jms/queue/deadletter";

	// Konstant for meldingsflytservices for PeSys
	public static final String KONF_SEND_DIALOGUE_ONLINE_PE = "jms/queue/dialogueOnlinePE";
	public static final String KONF_MOTTAK_DIALOGUE_ONLINE_PE = "jms/queue/brevserverMottakOnlinePE";
	public static final String KONF_MOTTAK_DIALOGUE_ARKIV_PE = "jms/queue/brevserverMottakArkivPE";
	public static final String KONF_MOTTAK_SAKSBEH_ONLINE_PE = "jms/queue/brevserverOnlinebrevPE";
	public static final String KONF_DEAD_LETTER_PE = "jms/queue/deadletterPE";
	public static final String KONF_SEND_REPLY_PE = "jms/queue/brevReplyQueuePE";

	// Feilkoder
	public static final String FEIL_IKKE_SYSTEM_TILGANG = "90000000 Ikke tilgang";
	public static final String FEIL_UKJENT = "90000001 Ukjent feil";
	public static final String FEIL_MELDING_UGYLDIG = "90000002 Meldingen var ikke gyldig";
	public static final String FEIL_BREV_EKSISTERER = "90000003 Brevet eksisterer allerede";
	public static final String FEIL_TEKNISK_FEIL = "90000004 Teknisk feil";

	// Koder for Brevstatus Modus
	public static final String BREVMODUS_FRALAGER = "frabrevlager";

	public static final String SKRIVERTYPE_INGEN = "ingen";
	public static final String SKRIVERTYPE_SENTRAL = "sentral";
	public static final String SKRIVERTYPE_LOKAL = "lokal";

	// Feilnivåer fra brevpakken
	public static final String BREVPAKKE_FEILNIVA_FEIL = "8";

	// Standard tegnsett
	public static final String TEGNSETT_ISO = "ISO-8859-1";

	// SQL for å hente ut timestamp
	public static final String TIMESTAMP_SQL = "current timestamp";

	// Joark sikkerhetsrolle
	public static final String JOARK_SECURITY_ROLE = "JoarkWsClient";
}