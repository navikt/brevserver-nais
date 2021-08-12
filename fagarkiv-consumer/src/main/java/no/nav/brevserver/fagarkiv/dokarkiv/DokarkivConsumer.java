package no.nav.brevserver.fagarkiv.dokarkiv;

import no.nav.brevserver.fagarkiv.dokarkiv.model.OppdaterJournalpostRequest;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;

/**
 * Interface defining operations against Dokarkiv, moved from Joark.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public interface DokarkivConsumer {



	void oppdaterJournalpost(OppdaterJournalpostRequest request, String brevreferanse) throws BrevTechnicalException;

	/**
	 * Checks if a journalpost exists based on 'brevreferanse'
	 *
	 * @param brevReferanse
	 *            The identifier of the Dokument to retrieve.
	 * @return
	 * @throws BrevTechnicalException
	 *             If an exception occurs.
	 */
	boolean isJournalpost(String brevReferanse) throws BrevTechnicalException;

}
