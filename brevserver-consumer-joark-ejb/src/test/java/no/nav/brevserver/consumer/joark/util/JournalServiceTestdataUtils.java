package no.nav.brevserver.consumer.joark.util;

import no.nav.brevserver.server.common.vo.FilType;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.DokumentInfo;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Dokumentkategori;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Dokumentstatus;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Fagomrade;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.FaktiskDistribusjonskanal;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Fildetaljer;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Filtype;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.JournalpostDokumentInfoRelasjon;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalposttype;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalstatus;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Mottakskanal;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.TilknyttetJournalpostSom;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Utsendingskanal;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.VariantFormat;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Utility class for use with JournalService domain objects.
 *
 * @author Marius Thoring, Visma Sirius
 */
public final class JournalServiceTestdataUtils {

	private JournalServiceTestdataUtils() {
	}

	public static XMLGregorianCalendar getXmlGregorianCalendarDate(int year, int month, int day) {
		Calendar calInstance = Calendar.getInstance();
		calInstance.set(year, (month - 1), day); // Month is 0-based
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(calInstance.getTime());
		XMLGregorianCalendar xmlGregorianCalendar = null;
		try {
			xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
		return xmlGregorianCalendar;
	}

	public static Journalposttype getJournalposttype(String kode) {
		Journalposttype journalposttype = new Journalposttype();
		journalposttype.setKode(kode);
		return journalposttype;
	}

	public static Fagomrade getFagomrade(String kode) {
		Fagomrade fagomrade = new Fagomrade();
		fagomrade.setKode(kode);
		return fagomrade;
	}

	public static Journalstatus getJournalstatus(String kode) {
		Journalstatus journalstatus = new Journalstatus();
		journalstatus.setKode(kode);
		return journalstatus;
	}

	public static Mottakskanal getMottakskanal(String kode) {
		Mottakskanal mottakskanal = new Mottakskanal();
		mottakskanal.setKode(kode);
		return mottakskanal;
	}

	public static Utsendingskanal getUtsendingskanal(String kode) {
		Utsendingskanal utsendingskanal = new Utsendingskanal();
		utsendingskanal.setKode(kode);
		return utsendingskanal;
	}

	public static FaktiskDistribusjonskanal getFaktiskDistribusjonskanal(String kode) {
		FaktiskDistribusjonskanal faktiskDistribusjonskanal = new FaktiskDistribusjonskanal();
		faktiskDistribusjonskanal.setKode(kode);
		return faktiskDistribusjonskanal;
	}

	public static TilknyttetJournalpostSom getTilknyttetJournalpostSom(String kode) {
		TilknyttetJournalpostSom tilknyttetJournalpostSom = new TilknyttetJournalpostSom();
		tilknyttetJournalpostSom.setKode(kode);
		return tilknyttetJournalpostSom;
	}

	public static Dokumentkategori getDokumentkategori(String kode) {
		Dokumentkategori dokumentkategori = new Dokumentkategori();
		dokumentkategori.setKode(kode);
		return dokumentkategori;
	}

	public static Dokumentstatus getDokumentstatus(String kode) {
		Dokumentstatus dokumentstatus = new Dokumentstatus();
		dokumentstatus.setKode(kode);
		return dokumentstatus;
	}

	public static Filtype getFiltype(String kode) {
		Filtype filtype = new Filtype();
		filtype.setKode(kode);
		return filtype;
	}

	public static VariantFormat getVariantFormat(String kode) {
		VariantFormat variantFormat = new VariantFormat();
		variantFormat.setKode(kode);
		return variantFormat;
	}

	public static Journalpost createJournalpost() {
		Journalpost journalpost = new Journalpost();

		journalpost.getJournalpostDokumentInfoRelasjonListe().add(createDokInfoRel());

		// Attributes:
		journalpost.setJournalpostId(1L);
		journalpost.setJournalposttype(getJournalposttype("M"));
		journalpost.setFagomrade(getFagomrade("PEN"));
		journalpost.setKravtype("A");
		journalpost.setJournalDato(getXmlGregorianCalendarDate(2011, 1, 1));
		journalpost.setDokumentDato(getXmlGregorianCalendarDate(2011, 1, 2));
		journalpost.setJournalstatus(getJournalstatus("U"));
		journalpost.setInnhold("innhold");
		journalpost.setAvsenderMottaker("avsenderMottaker");
		journalpost.setAvsenderMottakerId("2");
		journalpost.setJournalfortAvNavn("Julenissen");
		journalpost.setMottattDato(getXmlGregorianCalendarDate(2011, 1, 3));
		journalpost.setMottakskanal(getMottakskanal("EESSI"));
		journalpost.setUtsendingskanal(getUtsendingskanal("PSELV"));
		journalpost.setJournalForendeEnhetId("3");
		journalpost.setFordeling("fordeling");
		journalpost.setLand("land");
		journalpost.setOriginaltBestilt(true);
		journalpost.setFaktiskDistribusjonskanal(getFaktiskDistribusjonskanal("E"));
		journalpost.setElektroniskDistribusjon(true);
		journalpost.setSendtPrintDato(getXmlGregorianCalendarDate(2011, 1, 4));
		journalpost.setEkspedertDato(getXmlGregorianCalendarDate(2011, 1, 5));
		journalpost.setLestDato(getXmlGregorianCalendarDate(2011, 1, 6));
		journalpost.setMottattAdressatDato(getXmlGregorianCalendarDate(2011, 1, 7));
		journalpost.setAntallRetur(2L);
		journalpost.setAvsendtReturDato(getXmlGregorianCalendarDate(2011, 1, 8));
		journalpost.setMerknad("merknad");
		journalpost.setVersjon(1L);
		return journalpost;
	}

	public static JournalpostDokumentInfoRelasjon createDokInfoRel() {
		JournalpostDokumentInfoRelasjon jpDokInfoRel = new JournalpostDokumentInfoRelasjon();

		jpDokInfoRel.setDokumentInfo(createDokumentInfo());

		// Attributes:
		jpDokInfoRel.setJournalpostDokumentInfoRelasjonId(2L);
		jpDokInfoRel.setTilknyttetJournalpostSom(getTilknyttetJournalpostSom("HOVEDDOKUMENT"));
		jpDokInfoRel.setTilknyttetAvNavn("Askeladden");
		jpDokInfoRel.setVersjon("1");

		return jpDokInfoRel;
	}

	public static DokumentInfo createDokumentInfo() {
		DokumentInfo dokumentInfo = new DokumentInfo();

		dokumentInfo.getFildetaljerListe().addAll(createFilDetaljer());

		// Attributes:
		dokumentInfo.setDokumentInfoId(3L);
		dokumentInfo.setKategori(getDokumentkategori("B"));
		dokumentInfo.setDokumentFerdigDato(getXmlGregorianCalendarDate(2011, 1, 9));
		dokumentInfo.setDokumentstatus(getDokumentstatus("UNDER_REDIGERING"));
		dokumentInfo.setTittel("tittel");
		dokumentInfo.setBrevkode("brevkode");
		dokumentInfo.setBrevgruppe("brevgruppe");
		dokumentInfo.setOriginalJournalpostId(33L);
		dokumentInfo.setKonfidensialitet("konfidensialitet");
		dokumentInfo.setSensitivt(true);
		dokumentInfo.setIntegritet("integritet");
		dokumentInfo.setTilgjengelighet("tilgjengelighet");
		dokumentInfo.setInnskrenketPartsinnsyn(true);
		dokumentInfo.setOrganInternt(true);
		dokumentInfo.setKonvertertFraSystem("konvertertFraSystem");
		dokumentInfo.setVersjon(1L);

		return dokumentInfo;
	}

	public static List<Fildetaljer> createFilDetaljer() {
		Fildetaljer rtfFilDetaljer = new Fildetaljer();
		rtfFilDetaljer.setFildetaljerId(4L);
		rtfFilDetaljer.setFilUuid("filUuid");
		rtfFilDetaljer.setFiltype(getFiltype(FilType.RTF.getJoarkCode()));
		rtfFilDetaljer.setVariantFormat(getVariantFormat("PRODUKSJON"));
		rtfFilDetaljer.setBatchNavn("batchNavn");
		rtfFilDetaljer.setFilnavn("fil.rtf");
		rtfFilDetaljer.setFilstorrelse("30");
		rtfFilDetaljer.setVersjon(1L);

		Fildetaljer pdfFilDetaljer = new Fildetaljer();
		pdfFilDetaljer.setFildetaljerId(4L);
		pdfFilDetaljer.setFilUuid("filUuid");
		pdfFilDetaljer.setFiltype(getFiltype(FilType.PDF.getJoarkCode()));
		pdfFilDetaljer.setVariantFormat(getVariantFormat("ARKIV"));
		pdfFilDetaljer.setBatchNavn("batchNavn");
		pdfFilDetaljer.setFilnavn("fil.pdf");
		pdfFilDetaljer.setFilstorrelse("20");
		pdfFilDetaljer.setVersjon(1L);

		return Arrays.asList(rtfFilDetaljer, pdfFilDetaljer);
	}

	public static void clearFildetaljerListe(Journalpost journalpost) {
		journalpost.getJournalpostDokumentInfoRelasjonListe()
				.iterator().next().getDokumentInfo().getFildetaljerListe().clear();
	}

	public static void addFildetaljer(Journalpost journalpost, String filUuid, String variantFormat, String filtype) {
		List<Fildetaljer> fildetaljerListe = journalpost.getJournalpostDokumentInfoRelasjonListe()
				.iterator().next().getDokumentInfo().getFildetaljerListe();
		Fildetaljer fildetaljer = new Fildetaljer();
		fildetaljer.setVariantFormat(JournalServiceTestdataUtils.getVariantFormat(variantFormat));
		fildetaljer.setFiltype(JournalServiceTestdataUtils.getFiltype(filtype));
		fildetaljer.setFilUuid(filUuid);
		fildetaljer.setFilstorrelse("1");
		fildetaljerListe.add(fildetaljer);
	}
}
