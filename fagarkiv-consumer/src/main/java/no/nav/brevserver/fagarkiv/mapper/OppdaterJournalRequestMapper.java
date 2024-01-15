package no.nav.brevserver.fagarkiv.mapper;

import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Kodetabell;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Bruker;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.DokumentInfo;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Fildetaljer;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.JournalpostDokumentInfoRelasjon;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.OppdaterJournalRequest;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Saksrelasjon;

import java.util.List;

public class OppdaterJournalRequestMapper {

	public static OppdaterJournalRequest map(Journalpost journalpost) {
		OppdaterJournalRequest oppdaterJournalRequest = new OppdaterJournalRequest();

		oppdaterJournalRequest.setAntallRetur(journalpost.getAntallRetur());
		oppdaterJournalRequest.setAvsenderMottaker(journalpost.getAvsenderMottaker());
		oppdaterJournalRequest.setAvsenderMottakerId(journalpost.getAvsenderMottakerId());
		oppdaterJournalRequest.setAvsendtReturDato(journalpost.getAvsendtReturDato());
		oppdaterJournalRequest.setDokumentDato(journalpost.getDokumentDato());
		oppdaterJournalRequest.setElektroniskDistribusjon(journalpost.isElektroniskDistribusjon());
		oppdaterJournalRequest.setEkspedertDato(journalpost.getEkspedertDato());
		oppdaterJournalRequest.setFagomradeKode(map(journalpost.getFagomrade()));
		oppdaterJournalRequest.setFaktiskDistribusjonskanalKode(map(journalpost.getFaktiskDistribusjonskanal()));
		oppdaterJournalRequest.setFordeling(journalpost.getFordeling());
		oppdaterJournalRequest.setInnhold(journalpost.getInnhold());
		oppdaterJournalRequest.setJournalForendeEnhetId(journalpost.getJournalForendeEnhetId());
		oppdaterJournalRequest.setJournalpostId(journalpost.getJournalpostId());
		oppdaterJournalRequest.setJournalposttypeKode(map(journalpost.getJournalposttype()));
		oppdaterJournalRequest.setJournalstatusKode(map(journalpost.getJournalstatus()));
		oppdaterJournalRequest.setKravtype(journalpost.getKravtype());
		oppdaterJournalRequest.setLand(journalpost.getLand());
		oppdaterJournalRequest.setLestDato(journalpost.getLestDato());
		oppdaterJournalRequest.setMerknad(journalpost.getMerknad());
		oppdaterJournalRequest.setMottakskanalKode(map(journalpost.getMottakskanal()));
		oppdaterJournalRequest.setUtsendingskanalKode(map(journalpost.getUtsendingskanal()));
		oppdaterJournalRequest.setMottattAdressatDato(journalpost.getMottattAdressatDato());
		oppdaterJournalRequest.setMottattDato(journalpost.getMottattDato());
		oppdaterJournalRequest.setOriginaltBestilt(journalpost.isOriginaltBestilt());
		oppdaterJournalRequest.setSendtPrintDato(journalpost.getSendtPrintDato());
		oppdaterJournalRequest.setVersjon(journalpost.getVersjon());
		oppdaterJournalRequest.getJournalpostDokumentInfoRelasjonListe().addAll(map(journalpost.getJournalpostDokumentInfoRelasjonListe()));
		oppdaterJournalRequest.getGjelderListe().addAll(journalpost.getGjelderListe().stream().map(OppdaterJournalRequestMapper::map).toList());
		oppdaterJournalRequest.setSaksrelasjon(map(journalpost.getSaksrelasjon()));

		return oppdaterJournalRequest;
	}

	private static Saksrelasjon map(no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Saksrelasjon saksrelasjonInn) {
		Saksrelasjon saksrelasjon = new Saksrelasjon();

		saksrelasjon.setFagsystemKode(map(saksrelasjonInn.getFagsystem()));
		saksrelasjon.setFeilregistrert(saksrelasjonInn.isFeilregistrert());
		saksrelasjon.setSakId(saksrelasjonInn.getSakId());
		saksrelasjon.setSaksrelasjonId(saksrelasjonInn.getSaksrelasjonId());
		saksrelasjon.setVersjon(saksrelasjonInn.getVersjon());

		return saksrelasjon;
	}

	private static Bruker map(no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Bruker brukerInn) {
		Bruker bruker = new Bruker();

		bruker.setBrukerId(brukerInn.getBrukerId());
		bruker.setBrukerInfoId(brukerInn.getBrukerInfoId());
		bruker.setBrukertypeKode(map(brukerInn.getBrukertype()));
		bruker.setVersjon(brukerInn.getVersjon());

		return bruker;
	}


	private static List<JournalpostDokumentInfoRelasjon> map(List<no.nav.virksomhet.gjennomforing.arkiv.journal.v2.JournalpostDokumentInfoRelasjon> dokumentInfoRelasjonListe) {
		return dokumentInfoRelasjonListe.stream()
				.map(OppdaterJournalRequestMapper::map)
				.toList();
	}

	private static JournalpostDokumentInfoRelasjon map(no.nav.virksomhet.gjennomforing.arkiv.journal.v2.JournalpostDokumentInfoRelasjon relasjon) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = new JournalpostDokumentInfoRelasjon();
		journalpostDokumentInfoRelasjon.setJournalpostDokumentInfoRelasjonId(relasjon.getJournalpostDokumentInfoRelasjonId());
		journalpostDokumentInfoRelasjon.setDokumentInfo(map(relasjon.getDokumentInfo()));
		journalpostDokumentInfoRelasjon.setTilknyttetJournalpostSomKode(map(relasjon.getTilknyttetJournalpostSom()));
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(relasjon.getTilknyttetAvNavn());
		journalpostDokumentInfoRelasjon.setVersjon(Long.parseLong(relasjon.getVersjon()));

		return journalpostDokumentInfoRelasjon;
	}

	private static DokumentInfo map(no.nav.virksomhet.gjennomforing.arkiv.journal.v2.DokumentInfo info) {
		DokumentInfo dokumentInfo = new DokumentInfo();
		dokumentInfo.setDokumentInfoId(info.getDokumentInfoId());

		dokumentInfo.getFildetaljerListe().addAll(info.getFildetaljerListe().stream()
				.map(OppdaterJournalRequestMapper::map)
				.toList());

		dokumentInfo.setKategoriKode(map(info.getKategori()));
		dokumentInfo.setDokumentFerdigDato(info.getDokumentFerdigDato());
		dokumentInfo.setDokumentstatusKode(map(info.getDokumentstatus()));
		dokumentInfo.setTittel(info.getTittel());
		dokumentInfo.setBrevkode(info.getBrevkode());
		dokumentInfo.setBrevgruppe(info.getBrevgruppe());
		dokumentInfo.setKonfidensialitet(info.getKonfidensialitet());
		dokumentInfo.setSensitivt(info.isSensitivt());
		dokumentInfo.setIntegritet(info.getIntegritet());
		dokumentInfo.setTilgjengelighet(info.getTilgjengelighet());
		dokumentInfo.setInnskrenketPartsinnsyn(info.isInnskrenketPartsinnsyn());
		dokumentInfo.setOrganInternt(info.isOrganInternt());
		dokumentInfo.setVersjon(info.getVersjon());

		return dokumentInfo;
	}


	private static Fildetaljer map(no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Fildetaljer detaljer) {
		Fildetaljer fildetaljer = new Fildetaljer();
		fildetaljer.setBatchNavn(detaljer.getBatchNavn());
		fildetaljer.setFildetaljerId(detaljer.getFildetaljerId());
		fildetaljer.setFilnavn(detaljer.getFilnavn());
		fildetaljer.setFiltypeKode(map(detaljer.getFiltype()));
		fildetaljer.setVariantFormatKode(map(detaljer.getVariantFormat()));
		fildetaljer.setVersjon(detaljer.getVersjon());

		return fildetaljer;
	}

	private static String map(Kodetabell kodetabell) {
		return kodetabell.getKode() != null ? kodetabell.getKode() : null;
	}

}
