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
		oppdaterJournalRequest.setFagomradeKode(mapKodetabell(journalpost.getFagomrade()));
		oppdaterJournalRequest.setFaktiskDistribusjonskanalKode(mapKodetabell(journalpost.getFaktiskDistribusjonskanal()));
		oppdaterJournalRequest.setFordeling(journalpost.getFordeling());
		oppdaterJournalRequest.setInnhold(journalpost.getInnhold());
		oppdaterJournalRequest.setJournalForendeEnhetId(journalpost.getJournalForendeEnhetId());
		oppdaterJournalRequest.setJournalpostId(journalpost.getJournalpostId());
		oppdaterJournalRequest.setJournalposttypeKode(mapKodetabell(journalpost.getJournalposttype()));
		oppdaterJournalRequest.setJournalstatusKode(mapKodetabell(journalpost.getJournalstatus()));
		oppdaterJournalRequest.setKravtype(journalpost.getKravtype());
		oppdaterJournalRequest.setLand(journalpost.getLand());
		oppdaterJournalRequest.setLestDato(journalpost.getLestDato());
		oppdaterJournalRequest.setMerknad(journalpost.getMerknad());
		oppdaterJournalRequest.setMottakskanalKode(mapKodetabell(journalpost.getMottakskanal()));
		oppdaterJournalRequest.setUtsendingskanalKode(mapKodetabell(journalpost.getUtsendingskanal()));
		oppdaterJournalRequest.setMottattAdressatDato(journalpost.getMottattAdressatDato());
		oppdaterJournalRequest.setMottattDato(journalpost.getMottattDato());
		oppdaterJournalRequest.setOriginaltBestilt(journalpost.isOriginaltBestilt());
		oppdaterJournalRequest.setSendtPrintDato(journalpost.getSendtPrintDato());
		oppdaterJournalRequest.setVersjon(journalpost.getVersjon());

		oppdaterJournalRequest.getJournalpostDokumentInfoRelasjonListe()
				.addAll(mapJournalpostDokumentInfoRelasjonListe(journalpost.getJournalpostDokumentInfoRelasjonListe()));

		oppdaterJournalRequest.getGjelderListe()
				.addAll(mapGjelderliste(journalpost.getGjelderListe()));

		oppdaterJournalRequest.setSaksrelasjon(mapSaksrelasjon(journalpost.getSaksrelasjon()));

		return oppdaterJournalRequest;
	}

	private static List<Bruker> mapGjelderliste(List<no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Bruker> gjelderListe) {
		return gjelderListe.stream()
				.map(OppdaterJournalRequestMapper::mapBruker)
				.toList();
	}

	private static Saksrelasjon mapSaksrelasjon(no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Saksrelasjon saksrelasjonInn) {
		if (saksrelasjonInn == null)
			return null;

		Saksrelasjon saksrelasjon = new Saksrelasjon();

		saksrelasjon.setFagsystemKode(mapKodetabell(saksrelasjonInn.getFagsystem()));
		saksrelasjon.setFeilregistrert(saksrelasjonInn.isFeilregistrert());
		saksrelasjon.setSakId(saksrelasjonInn.getSakId());
		saksrelasjon.setSaksrelasjonId(saksrelasjonInn.getSaksrelasjonId());
		saksrelasjon.setVersjon(saksrelasjonInn.getVersjon());

		return saksrelasjon;
	}

	private static Bruker mapBruker(no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Bruker brukerInn) {
		if (brukerInn == null)
			return null;

		Bruker bruker = new Bruker();

		bruker.setBrukerId(brukerInn.getBrukerId());
		bruker.setBrukerInfoId(brukerInn.getBrukerInfoId());
		bruker.setBrukertypeKode(mapKodetabell(brukerInn.getBrukertype()));
		bruker.setVersjon(brukerInn.getVersjon());

		return bruker;
	}


	private static List<JournalpostDokumentInfoRelasjon> mapJournalpostDokumentInfoRelasjonListe(List<no.nav.virksomhet.gjennomforing.arkiv.journal.v2.JournalpostDokumentInfoRelasjon> dokumentInfoRelasjonListe) {
		return dokumentInfoRelasjonListe.stream()
				.map(OppdaterJournalRequestMapper::mapJournalpostDokumentInfoRelasjon)
				.toList();
	}

	private static JournalpostDokumentInfoRelasjon mapJournalpostDokumentInfoRelasjon(no.nav.virksomhet.gjennomforing.arkiv.journal.v2.JournalpostDokumentInfoRelasjon relasjon) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = new JournalpostDokumentInfoRelasjon();

		journalpostDokumentInfoRelasjon.setJournalpostDokumentInfoRelasjonId(relasjon.getJournalpostDokumentInfoRelasjonId());
		journalpostDokumentInfoRelasjon.setDokumentInfo(mapDokumentInfo(relasjon.getDokumentInfo()));
		journalpostDokumentInfoRelasjon.setTilknyttetJournalpostSomKode(mapKodetabell(relasjon.getTilknyttetJournalpostSom()));
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(relasjon.getTilknyttetAvNavn());
		journalpostDokumentInfoRelasjon.setVersjon(Long.parseLong(relasjon.getVersjon()));

		return journalpostDokumentInfoRelasjon;
	}

	private static DokumentInfo mapDokumentInfo(no.nav.virksomhet.gjennomforing.arkiv.journal.v2.DokumentInfo info) {
		DokumentInfo dokumentInfo = new DokumentInfo();
		dokumentInfo.setDokumentInfoId(info.getDokumentInfoId());

		dokumentInfo.getFildetaljerListe().addAll(info.getFildetaljerListe().stream()
				.map(OppdaterJournalRequestMapper::mapFildetaljer)
				.toList());

		dokumentInfo.setKategoriKode(mapKodetabell(info.getKategori()));
		dokumentInfo.setDokumentFerdigDato(info.getDokumentFerdigDato());
		dokumentInfo.setDokumentstatusKode(mapKodetabell(info.getDokumentstatus()));
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


	private static Fildetaljer mapFildetaljer(no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Fildetaljer detaljer) {
		Fildetaljer fildetaljer = new Fildetaljer();
		fildetaljer.setBatchNavn(detaljer.getBatchNavn());
		fildetaljer.setFildetaljerId(detaljer.getFildetaljerId());
		fildetaljer.setFilnavn(detaljer.getFilnavn());
		fildetaljer.setFiltypeKode(mapKodetabell(detaljer.getFiltype()));
		fildetaljer.setVariantFormatKode(mapKodetabell(detaljer.getVariantFormat()));
		fildetaljer.setVersjon(detaljer.getVersjon());

		return fildetaljer;
	}

	private static String mapKodetabell(Kodetabell kodetabell) {
		if (kodetabell == null)
			return null;

		return kodetabell.getKode() != null ? kodetabell.getKode() : null;
	}

}
