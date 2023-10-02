package no.nav.brevserver.joark;

import no.nav.brevserver.core.exception.BrevserverTechnicalException;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.OppdaterJournalRequest;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.OppdaterJournal;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class JournalbehandlingClient extends WebServiceGatewaySupport {

	public void oppdaterJournalpost(OppdaterJournalRequest oppdaterJournalRequest) {
		OppdaterJournal oppdaterJournal = new OppdaterJournal();
		try {
			oppdaterJournal.setRequest(oppdaterJournalRequest);
			getWebServiceTemplate()
					.marshalSendAndReceive(oppdaterJournal);
		} catch (Exception e) {
			throw new BrevserverTechnicalException("Klarte ikke oppdatere journalpost med journalpostId=" + oppdaterJournalRequest.getJournalpostId(), e);
		}
	}
}
