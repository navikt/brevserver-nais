package no.nav.brevserver.consumer.joark.map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import no.nav.brevserver.consumer.joark.map.OppdaterJournalRequestMapper;
import no.nav.brevserver.consumer.joark.util.JournalServiceTestdataUtils;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.DokumentInfo;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Fildetaljer;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.JournalpostDokumentInfoRelasjon;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.OppdaterJournalRequest;

import org.junit.Test;

/**
 * Unit tests for OppdaterJournalRequestMapper. 
 * 
 * @author Marius Thøring, Visma Sirius
 */
public class OppdaterJournalRequestMapperTest {

	@Test
	public void shouldMapJournalpostToOppdaterJournalRequestCorrectly() {
		Journalpost fromJournalpost = JournalServiceTestdataUtils.createJournalpost();
		OppdaterJournalRequestMapper oppdaterJournalRequestMapper = new OppdaterJournalRequestMapper();
		
		OppdaterJournalRequest toRequest = oppdaterJournalRequestMapper.map(fromJournalpost);

		assertOppdaterJournalRequest(toRequest, fromJournalpost);
	}

	private void assertOppdaterJournalRequest(OppdaterJournalRequest toRequest, Journalpost fromJournalpost) {
		assertThat(toRequest.getJournalpostDokumentInfoRelasjonListe().size(), is(1));
		assertJournalpostDokumentInfoRelasjon(toRequest.getJournalpostDokumentInfoRelasjonListe().get(0), 
				fromJournalpost.getJournalpostDokumentInfoRelasjonListe().get(0));
		
		assertThat(toRequest.getAntallRetur(), is(fromJournalpost.getAntallRetur()));
		assertThat(toRequest.getAvsenderMottaker(), is(fromJournalpost.getAvsenderMottaker()));
		assertThat(toRequest.getAvsenderMottakerId(), is(fromJournalpost.getAvsenderMottakerId()));
		assertThat(toRequest.getAvsendtReturDato(), is(fromJournalpost.getAvsendtReturDato()));
		assertThat(toRequest.getDokumentDato(), is(fromJournalpost.getDokumentDato()));
		assertThat(toRequest.isElektroniskDistribusjon(), is(fromJournalpost.isElektroniskDistribusjon()));
		assertThat(toRequest.getEkspedertDato(), is(fromJournalpost.getEkspedertDato()));
		assertThat(toRequest.getFagomradeKode(), is(fromJournalpost.getFagomrade().getKode()));
		assertThat(toRequest.getFaktiskDistribusjonskanalKode(), is(fromJournalpost.getFaktiskDistribusjonskanal().getKode()));
		assertThat(toRequest.getFordeling(), is(fromJournalpost.getFordeling()));
		assertThat(toRequest.getInnhold(), is(fromJournalpost.getInnhold()));
		assertThat(toRequest.getJournalForendeEnhetId(), is(fromJournalpost.getJournalForendeEnhetId()));
		assertThat(toRequest.getJournalpostId(), is(fromJournalpost.getJournalpostId()));
		assertThat(toRequest.getJournalposttypeKode(), is(fromJournalpost.getJournalposttype().getKode()));
		assertThat(toRequest.getJournalstatusKode(), is(fromJournalpost.getJournalstatus().getKode()));
		assertThat(toRequest.getKravtype(), is(fromJournalpost.getKravtype()));
		assertThat(toRequest.getLand(), is(fromJournalpost.getLand()));
		assertThat(toRequest.getLestDato(), is(fromJournalpost.getLestDato()));
		assertThat(toRequest.getMerknad(), is(fromJournalpost.getMerknad()));
		assertThat(toRequest.getMottakskanalKode(), is(fromJournalpost.getMottakskanal().getKode()));
		assertThat(toRequest.getUtsendingskanalKode(), is(fromJournalpost.getUtsendingskanal().getKode()));
		assertThat(toRequest.getMottattAdressatDato(), is(fromJournalpost.getMottattAdressatDato()));
		assertThat(toRequest.getMottattDato(), is(fromJournalpost.getMottattDato()));
		assertThat(toRequest.isOriginaltBestilt(), is(fromJournalpost.isOriginaltBestilt()));
		assertThat(toRequest.getSendtPrintDato(), is(fromJournalpost.getSendtPrintDato()));
		assertThat(toRequest.getVersjon(), is(fromJournalpost.getVersjon()));
		
		//Should not be set
		assertThat(toRequest.getEndretAvNavn(), is(nullValue()));
		assertThat(toRequest.getGjelderListe().isEmpty(), is(true));
		assertThat(toRequest.getSaksrelasjon(), is(nullValue()));
		assertThat(toRequest.getKryssreferanseListe().isEmpty(), is(true));
		assertThat(toRequest.getReturInfoListe().isEmpty(), is(true));
	}

	private void assertJournalpostDokumentInfoRelasjon(
			no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.JournalpostDokumentInfoRelasjon toJpDokRel, 
			JournalpostDokumentInfoRelasjon fromJpDokRel) {
		assertDokumentInfo(toJpDokRel.getDokumentInfo(), fromJpDokRel.getDokumentInfo());

		assertThat(toJpDokRel.getJournalpostDokumentInfoRelasjonId(), is(fromJpDokRel.getJournalpostDokumentInfoRelasjonId()));
		assertThat(toJpDokRel.getTilknyttetJournalpostSomKode(), is(fromJpDokRel.getTilknyttetJournalpostSom().getKode()));
		assertThat(toJpDokRel.getTilknyttetAvNavn(), is(fromJpDokRel.getTilknyttetAvNavn()));
		assertThat(toJpDokRel.getVersjon(), is(Long.valueOf(fromJpDokRel.getVersjon())));
	}

	private void assertDokumentInfo(
			no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.DokumentInfo toDokumentInfo,
			DokumentInfo fromDokumentInfo) {
		assertThat(toDokumentInfo.getFildetaljerListe().size(), is(2));
		assertFilDetaljer(toDokumentInfo.getFildetaljerListe().get(0), fromDokumentInfo.getFildetaljerListe().get(0));
		assertFilDetaljer(toDokumentInfo.getFildetaljerListe().get(1), fromDokumentInfo.getFildetaljerListe().get(1));
		
		assertThat(toDokumentInfo.getDokumentInfoId(), is(fromDokumentInfo.getDokumentInfoId()));
		assertThat(toDokumentInfo.getKategoriKode(), is(fromDokumentInfo.getKategori().getKode()));
		assertThat(toDokumentInfo.getDokumentFerdigDato(), is(fromDokumentInfo.getDokumentFerdigDato()));
		assertThat(toDokumentInfo.getDokumentstatusKode(), is(fromDokumentInfo.getDokumentstatus().getKode()));
		assertThat(toDokumentInfo.getTittel(), is(fromDokumentInfo.getTittel()));
		assertThat(toDokumentInfo.getBrevkode(), is(fromDokumentInfo.getBrevkode()));
		assertThat(toDokumentInfo.getBrevgruppe(), is(fromDokumentInfo.getBrevgruppe()));
		assertThat(toDokumentInfo.getKonfidensialitet(), is(fromDokumentInfo.getKonfidensialitet()));
		assertThat(toDokumentInfo.isSensitivt(), is(fromDokumentInfo.isSensitivt()));
		assertThat(toDokumentInfo.getIntegritet(), is(fromDokumentInfo.getIntegritet()));
		assertThat(toDokumentInfo.getTilgjengelighet(), is(fromDokumentInfo.getTilgjengelighet()));
		assertThat(toDokumentInfo.isInnskrenketPartsinnsyn(), is(fromDokumentInfo.isInnskrenketPartsinnsyn()));
		assertThat(toDokumentInfo.isOrganInternt(), is(fromDokumentInfo.isOrganInternt()));
		assertThat(toDokumentInfo.getVersjon(), is(fromDokumentInfo.getVersjon()));
		
		//Should not be set
		assertThat(toDokumentInfo.getSkannetInnholdListe().isEmpty(), is(true));
	}

	private void assertFilDetaljer(
			no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Fildetaljer toFilDetaljer,
			Fildetaljer fromFilDetaljer) {
		
		assertThat(toFilDetaljer.getBatchNavn(), is(fromFilDetaljer.getBatchNavn()));
		assertThat(toFilDetaljer.getFildetaljerId(), is(fromFilDetaljer.getFildetaljerId()));
		assertThat(toFilDetaljer.getFilnavn(), is(fromFilDetaljer.getFilnavn()));
		assertThat(toFilDetaljer.getFiltypeKode(), is(fromFilDetaljer.getFiltype().getKode()));
		assertThat(toFilDetaljer.getVariantFormatKode(), is(fromFilDetaljer.getVariantFormat().getKode()));
		assertThat(toFilDetaljer.getVersjon(), is(fromFilDetaljer.getVersjon()));
	}
}
