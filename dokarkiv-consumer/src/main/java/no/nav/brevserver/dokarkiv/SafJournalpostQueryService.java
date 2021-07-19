package no.nav.brevserver.dokarkiv;

import no.nav.brevserver.dokarkiv.journalpost.Journalpost;

public interface SafJournalpostQueryService {
	Journalpost hentJournalpost(String journalpostid, String authorizationHeader);

}
