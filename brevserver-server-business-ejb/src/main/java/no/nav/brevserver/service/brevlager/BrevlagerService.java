package no.nav.brevserver.service.brevlager;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;

/**
 */

public interface BrevlagerService {

	/**
	 * Henter et brev baser. Brevet hentes fra Brevlageret (DB2).
	 *
	 * @param systemID
	 * @param brevReferanse
	 * @return
	 * @throws BrevTechnicalException
	 */
	BrevVO getBrev(String systemID, String brevReferanse) throws BrevTechnicalException;

	/**
	 * Lagrer et brev. Brevet lagres i Brevlageret (DB2).
	 *
	 * @param brev       Data som skal lagres i BREVLAGER-tabellen
	 * @param brevstatus Data som skal lagres i BREVSTATUS-tabellen
	 * @return
	 * @throws BrevTechnicalException
	 */
	BrevStatusVO lagreBrev(BrevVO brev, Brevstatus brevstatus, String token) throws BrevTechnicalException;

	/**
	 * Ferdigstiller et brev. Både kladd og ferdigstilt brev lagres i Brevlageret (DB2).
	 *
	 * @param brevstatus
	 * @param brevVORtf
	 * @param brev
	 * @throws BrevTechnicalException
	 */
	void ferdigstillBrev(Brevstatus brevstatus, BrevVO brevVORtf, BrevVO brev, String token) throws BrevTechnicalException;

	/**
	 * Checks if a call to the brevlager can be made
	 */
	void ping();
}
