package no.nav.brevserver.consumer.joark.map;

import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.OppdaterJournalRequest;

/**
 * Mapper for OppdaterJournalRequest.
 *
 * @author Marius Thøring, Visma Sirius
 */
public class OppdaterJournalRequestMapper extends AbstractDozerMapper {

	/**
	 * Maps from HentJournalpostRequest.Journalpost to OppdaterJournalRequest
	 * 
	 * @param journalpost The journalpost to map.
	 * @return a mapped OppdaterJournalRequest.
	 */
	
	public OppdaterJournalRequest map(Journalpost journalpost) {
		OppdaterJournalRequest oppdaterJournalRequest = new OppdaterJournalRequest();
		getDozerMapper().map(journalpost, oppdaterJournalRequest);
		
		return oppdaterJournalRequest;
	}
}
