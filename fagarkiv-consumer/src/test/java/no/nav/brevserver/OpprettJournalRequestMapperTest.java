package no.nav.brevserver;

import no.nav.brevserver.fagarkiv.mapper.OppdaterJournalRequestMapper;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Bruker;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Brukertype;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.DokumentInfo;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Dokumentkategori;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Dokumentstatus;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Fagomrade;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Fagsystem;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.FaktiskDistribusjonskanal;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Fildetaljer;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Filtype;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.JournalpostDokumentInfoRelasjon;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalposttype;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalstatus;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Kodetabell;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Mottakskanal;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Saksrelasjon;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.SkannetInnhold;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.TilknyttetJournalpostSom;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Utsendingskanal;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.VariantFormat;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.OppdaterJournalRequest;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;

import static org.assertj.core.api.Assertions.assertThat;

public class OpprettJournalRequestMapperTest {

	private static final ZoneId TIDSSONE_NORGE = ZoneId.of("Europe/Oslo");
	private static final GregorianCalendar CALENDAR = GregorianCalendar.from(LocalDateTime.now().atZone(TIDSSONE_NORGE));
	private static final XMLGregorianCalendar XML_GREGORIAN_CALENDAR;

	static {
		try {
			XML_GREGORIAN_CALENDAR = DatatypeFactory.newInstance().newXMLGregorianCalendar(CALENDAR);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void shouldMapJournalpost() {
		Journalpost journalpost = createJournalpost();

		OppdaterJournalRequest oppdaterJournalRequest = OppdaterJournalRequestMapper.map(journalpost);

		assertThat(oppdaterJournalRequest)
				.usingRecursiveComparison()
				.ignoringFields("endretAvNavn", "saksrelasjon")
				.ignoringFieldsMatchingRegexes(".*Kode", ".*Liste")
				.isEqualTo(journalpost);

		assertThat(oppdaterJournalRequest.getFagomradeKode()).isEqualTo(journalpost.getFagomrade().getKode());
		assertThat(oppdaterJournalRequest.getFaktiskDistribusjonskanalKode()).isEqualTo(journalpost.getFaktiskDistribusjonskanal().getKode());
		assertThat(oppdaterJournalRequest.getJournalposttypeKode()).isEqualTo(journalpost.getJournalposttype().getKode());
		assertThat(oppdaterJournalRequest.getJournalstatusKode()).isEqualTo(journalpost.getJournalstatus().getKode());
		assertThat(oppdaterJournalRequest.getMottakskanalKode()).isEqualTo(journalpost.getMottakskanal().getKode());
		assertThat(oppdaterJournalRequest.getUtsendingskanalKode()).isEqualTo(journalpost.getUtsendingskanal().getKode());

		assertJournalpostDokumentInfoRelasjonListe(oppdaterJournalRequest, journalpost);
		assertGjelderListe(oppdaterJournalRequest, journalpost);
		assertSaksrelasjon(oppdaterJournalRequest.getSaksrelasjon(), journalpost.getSaksrelasjon());
	}

	@Test
	void shouldNotMapExplicitlyHandledNullValues() {
		Journalpost journalpost = createJournalpost(null);
		journalpost.setSaksrelasjon(null);
		journalpost.setFagomrade(null);
		journalpost.setFaktiskDistribusjonskanal(null);
		journalpost.setJournalposttype(null);
		journalpost.setJournalstatus(null);
		journalpost.setMottakskanal(null);
		journalpost.setUtsendingskanal(null);

		OppdaterJournalRequest oppdaterJournalRequest = OppdaterJournalRequestMapper.map(journalpost);

		assertThat(oppdaterJournalRequest.getGjelderListe()).containsOnlyNulls();
		assertThat(oppdaterJournalRequest).extracting(
				"saksrelasjon", "fagomradeKode", "faktiskDistribusjonskanalKode", "journalposttypeKode", "journalstatusKode", "mottakskanalKode", "utsendingskanalKode")
				.containsOnlyNulls();
	}

	private void assertJournalpostDokumentInfoRelasjonListe(OppdaterJournalRequest request, Journalpost journalpost) {

		assertThat(request.getJournalpostDokumentInfoRelasjonListe())
				.hasSameSizeAs(journalpost.getJournalpostDokumentInfoRelasjonListe())
				.singleElement()
				.satisfies(journalpostDokumentInfoRelasjon ->
						assertJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon, journalpost.getJournalpostDokumentInfoRelasjonListe().getFirst()));
	}

	private void assertJournalpostDokumentInfoRelasjon(no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.JournalpostDokumentInfoRelasjon actual, JournalpostDokumentInfoRelasjon expected) {

		assertThat(actual.getJournalpostDokumentInfoRelasjonId()).isEqualTo(expected.getJournalpostDokumentInfoRelasjonId());
		assertThat(actual.getTilknyttetJournalpostSomKode()).isEqualTo(expected.getTilknyttetJournalpostSom().getKode());
		assertThat(actual.getTilknyttetAvNavn()).isEqualTo(expected.getTilknyttetAvNavn());
		assertThat(actual.getVersjon()).isEqualTo(Long.parseLong(expected.getVersjon()));

		assertDokumentInfo(actual.getDokumentInfo(), expected.getDokumentInfo());
	}

	private void assertDokumentInfo(no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.DokumentInfo actual, DokumentInfo expected) {

		assertThat(actual.getDokumentInfoId()).isEqualTo(expected.getDokumentInfoId());
		assertThat(actual.getKategoriKode()).isEqualTo(expected.getKategori().getKode());
		assertThat(actual.getDokumentFerdigDato()).isEqualTo(expected.getDokumentFerdigDato());
		assertThat(actual.getDokumentstatusKode()).isEqualTo(expected.getDokumentstatus().getKode());
		assertThat(actual.getTittel()).isEqualTo(expected.getTittel());
		assertThat(actual.getBrevkode()).isEqualTo(expected.getBrevkode());
		assertThat(actual.getBrevgruppe()).isEqualTo(expected.getBrevgruppe());
		assertThat(actual.getKonfidensialitet()).isEqualTo(expected.getKonfidensialitet());
		assertThat(actual.isSensitivt()).isEqualTo(expected.isSensitivt());
		assertThat(actual.getIntegritet()).isEqualTo(expected.getIntegritet());
		assertThat(actual.getTilgjengelighet()).isEqualTo(expected.getTilgjengelighet());
		assertThat(actual.isInnskrenketPartsinnsyn()).isEqualTo(expected.isInnskrenketPartsinnsyn());
		assertThat(actual.isOrganInternt()).isEqualTo(expected.isOrganInternt());
		assertThat(actual.getVersjon()).isEqualTo(expected.getVersjon());

		assertThat(actual.getFildetaljerListe())
				.hasSameSizeAs(expected.getFildetaljerListe())
				.singleElement()
				.satisfies(fildetaljer -> assertFildetaljer(fildetaljer, expected.getFildetaljerListe().getFirst()));
	}

	private void assertFildetaljer(no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Fildetaljer actual, Fildetaljer expected) {

		assertThat(actual.getBatchNavn()).isEqualTo(expected.getBatchNavn());
		assertThat(actual.getFildetaljerId()).isEqualTo(expected.getFildetaljerId());
		assertThat(actual.getFilnavn()).isEqualTo(expected.getFilnavn());
		assertThat(actual.getFiltypeKode()).isEqualTo(expected.getFiltype().getKode());
		assertThat(actual.getVariantFormatKode()).isEqualTo(expected.getVariantFormat().getKode());
		assertThat(actual.getVersjon()).isEqualTo(expected.getVersjon());
	}

	private void assertSaksrelasjon(no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Saksrelasjon actual, Saksrelasjon expected) {

		assertThat(actual.getFagsystemKode()).isEqualTo(expected.getFagsystem().getKode());
		assertThat(actual.isFeilregistrert()).isEqualTo(expected.isFeilregistrert());
		assertThat(actual.getSakId()).isEqualTo(expected.getSakId());
		assertThat(actual.getSaksrelasjonId()).isEqualTo(expected.getSaksrelasjonId());
		assertThat(actual.getVersjon()).isEqualTo(expected.getVersjon());
	}

	private void assertGjelderListe(OppdaterJournalRequest request, Journalpost journalpost) {

		assertThat(request.getGjelderListe())
				.hasSameSizeAs(journalpost.getGjelderListe())
				.singleElement()
				.satisfies(bruker -> assertBruker(bruker, journalpost.getGjelderListe().getFirst()));
	}

	private void assertBruker(no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Bruker actual, Bruker expected) {

		assertThat(actual.getBrukerId()).isEqualTo(expected.getBrukerId());
		assertThat(actual.getBrukerInfoId()).isEqualTo(expected.getBrukerInfoId());
		assertThat(actual.getBrukertypeKode()).isEqualTo(expected.getBrukertype().getKode());
		assertThat(actual.getVersjon()).isEqualTo(expected.getVersjon());
	}

	private Journalpost createJournalpost() {
		return createJournalpost(createBruker());
	}

	private Journalpost createJournalpost(Bruker bruker) {
		Journalpost journalpost = new Journalpost();

		journalpost.setJournalpostId(123L);
		journalpost.setSaksrelasjon(createSaksrelasjon());
		journalpost.getGjelderListe().add(bruker);
		journalpost.getJournalpostDokumentInfoRelasjonListe().add(createJournalpostDokumentInfoRelasjon());
		journalpost.setJournalposttype(createKodetabell(Journalposttype.class, "journalposttypeKode"));
		journalpost.setFagomrade(createKodetabell(Fagomrade.class, "fagomradeKode"));
		journalpost.setKravtype("kravtype");
		journalpost.setJournalDato(XML_GREGORIAN_CALENDAR);
		journalpost.setDokumentDato(XML_GREGORIAN_CALENDAR);
		journalpost.setJournalstatus(createKodetabell(Journalstatus.class, "journalstatusKode"));
		journalpost.setInnhold("innhold");
		journalpost.setAvsenderMottaker("avsenderMottaker");
		journalpost.setAvsenderMottakerId("avsenderMottakerId");
		journalpost.setJournalfortAvNavn("journalfortAvNavn");
		journalpost.setMottattDato(XML_GREGORIAN_CALENDAR);
		journalpost.setMottakskanal(createKodetabell(Mottakskanal.class, "mottakskanalKode"));
		journalpost.setUtsendingskanal(createKodetabell(Utsendingskanal.class, "utsendingskanalKode"));
		journalpost.setJournalForendeEnhetId("journalForendeEnhetId");
		journalpost.setFordeling("fordeling");
		journalpost.setLand("land");
		journalpost.setOriginaltBestilt(false);
		journalpost.setFaktiskDistribusjonskanal(createKodetabell(FaktiskDistribusjonskanal.class, "faktiskDistribusjonskanalKode"));
		journalpost.setElektroniskDistribusjon(false);
		journalpost.setSendtPrintDato(XML_GREGORIAN_CALENDAR);
		journalpost.setEkspedertDato(XML_GREGORIAN_CALENDAR);
		journalpost.setLestDato(XML_GREGORIAN_CALENDAR);
		journalpost.setMottattAdressatDato(XML_GREGORIAN_CALENDAR);
		journalpost.setAntallRetur(1L);
		journalpost.setAvsendtReturDato(XML_GREGORIAN_CALENDAR);
		journalpost.setMerknad("merknad");
		journalpost.setVersjon(123L);

		return journalpost;
	}

	private JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjon() {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = new JournalpostDokumentInfoRelasjon();
		journalpostDokumentInfoRelasjon.setJournalpostDokumentInfoRelasjonId(123L);
		journalpostDokumentInfoRelasjon.setDokumentInfo(createDokumentInfo());
		journalpostDokumentInfoRelasjon.setTilknyttetJournalpostSom(createKodetabell(TilknyttetJournalpostSom.class, "tilknyttetJournalpostSomKode"));
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn("tilknyttetAvNavn");
		journalpostDokumentInfoRelasjon.setVersjon("123");

		return journalpostDokumentInfoRelasjon;
	}

	private DokumentInfo createDokumentInfo() {
		DokumentInfo dokumentInfo = new DokumentInfo();
		dokumentInfo.setDokumentInfoId(123L);
		dokumentInfo.getSkannetInnholdListe().add(createSkannetInnhold());
		dokumentInfo.getFildetaljerListe().add(createFildetaljer());
		dokumentInfo.setKategori(createKodetabell(Dokumentkategori.class, "dokumentkategoriKode"));
		dokumentInfo.setDokumentFerdigDato(XML_GREGORIAN_CALENDAR);
		dokumentInfo.setDokumentstatus(createKodetabell(Dokumentstatus.class, "dokumentstatusKode"));
		dokumentInfo.setTittel("tittel");
		dokumentInfo.setBrevkode("brevkode");
		dokumentInfo.setBrevgruppe("brevgruppe");
		dokumentInfo.setOriginalJournalpostId(123L);
		dokumentInfo.setKonfidensialitet("konfidensialitet");
		dokumentInfo.setSensitivt(false);
		dokumentInfo.setIntegritet("integritet");
		dokumentInfo.setTilgjengelighet("tilgjengelighet");
		dokumentInfo.setInnskrenketPartsinnsyn(false);
		dokumentInfo.setOrganInternt(false);
		dokumentInfo.setKonvertertFraSystem("konvertertFraSystem");
		dokumentInfo.setVersjon(123L);

		return dokumentInfo;
	}

	private Fildetaljer createFildetaljer() {
		Fildetaljer fildetaljer = new Fildetaljer();
		fildetaljer.setFildetaljerId(123L);
		fildetaljer.setFilUuid("filUuid");
		fildetaljer.setFiltype(createKodetabell(Filtype.class, "filtypeKode"));
		fildetaljer.setVariantFormat(createKodetabell(VariantFormat.class, "variantFormatKode"));
		fildetaljer.setBatchNavn("batchNavn");
		fildetaljer.setFilnavn("filnavn");
		fildetaljer.setFilstorrelse("123");
		fildetaljer.setVersjon(123L);

		return fildetaljer;
	}

	private SkannetInnhold createSkannetInnhold() {
		SkannetInnhold skannetInnhold = new SkannetInnhold();
		skannetInnhold.setSkannetInnholdId(123L);
		skannetInnhold.setVedleggNr(1);
		skannetInnhold.setVedleggInnhold("vedleggInnhold");
		skannetInnhold.setVersjon(123L);

		return skannetInnhold;
	}

	private Bruker createBruker() {
		Bruker bruker = new Bruker();
		bruker.setBrukerId("brukerId");
		bruker.setBrukerInfoId(123L);
		bruker.setBrukertype(createKodetabell(Brukertype.class, "brukerTypeKode"));
		bruker.setVersjon(123L);

		return bruker;
	}

	private Saksrelasjon createSaksrelasjon() {
		Saksrelasjon saksrelasjon = new Saksrelasjon();
		saksrelasjon.setSaksrelasjonId(123L);
		saksrelasjon.setSakId("sakId");
		saksrelasjon.setFagsystem(createKodetabell(Fagsystem.class, "fagsystemKode"));
		saksrelasjon.setFeilregistrert(false);
		saksrelasjon.setVersjon(123L);

		return saksrelasjon;
	}

	private static <T extends Kodetabell> T createKodetabell(Class<T> clazz, String kode) {
		try {
			var kodetabell = clazz.getDeclaredConstructor().newInstance();
			kodetabell.setKode(kode);
			return kodetabell;
		} catch (Exception e) {
			throw new RuntimeException("Klarte ikke instansiere objekt av klasse " + clazz.getSimpleName(), e);
		}
	}

}
