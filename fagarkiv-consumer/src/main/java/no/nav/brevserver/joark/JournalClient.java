package no.nav.brevserver.joark;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentResponse;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.HentDokument;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.HentJournalpost;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.HentJournalpostResponse;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class JournalClient extends WebServiceGatewaySupport {

	public Journalpost hentJournalpost(Long brevreferanse) throws BrevTechnicalException {
		HentJournalpostRequest hentJournalpostRequest = new HentJournalpostRequest();
		hentJournalpostRequest.setJournalpostId(brevreferanse);
		HentJournalpost hentJournalpost = new HentJournalpost();
		hentJournalpost.setRequest(hentJournalpostRequest);
		try {
			HentJournalpostResponse hentJournalpostResponse = (HentJournalpostResponse) getWebServiceTemplate()
					.marshalSendAndReceive(hentJournalpost);
			return hentJournalpostResponse.getResponse().getJournalpost();
		} catch (Exception e) {
			throw new BrevTechnicalException("Klarte ikke hente journalpost: " + e.getMessage());
		}
	}

	public HentDokumentResponse hentDokument(HentDokumentRequest hentDokumentRequest) throws BrevTechnicalException {
		HentDokument hentDokument = new HentDokument();
		hentDokument.setRequest(hentDokumentRequest);
		try {
			no.nav.virksomhet.tjenester.arkiv.journal.v2.HentDokumentResponse response = (no.nav.virksomhet.tjenester.arkiv.journal.v2.HentDokumentResponse) getWebServiceTemplate().marshalSendAndReceive(hentDokument);
			return response.getResponse();
		} catch (Exception e) {
			throw new BrevTechnicalException("Klarte ikke hente journalpost: " + e.getMessage());
		}
	}
}
