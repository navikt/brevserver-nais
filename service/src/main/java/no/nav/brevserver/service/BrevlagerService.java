package no.nav.brevserver.service;

import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;

public interface BrevlagerService {
	/**
	 * Henter et brev baser. Brevet hentes fra Brevlageret (DB2).
	 *
	 * @param systemID
	 * @param brevReferanse
	 * @return
	 * @throws BrevTechnicalException
	 */

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
	BrevStatusVO lagreBrev(BrevVO brev, BrevStatusVO brevstatus) throws BrevTechnicalException;

	/**
	 * Ferdigstiller et brev. Både kladd og ferdigstilt brev lagres i Brevlageret (DB2).
	 *
	 * @param brevstatus
	 * @param brevVORtf
	 * @param brev
	 * @throws BrevTechnicalException
	 */
	void ferdigstillBrev(BrevStatusVO brevstatus, BrevVO brevVORtf, BrevVO brev) throws BrevException;

	BrevVO hentDokumentFromBrevlagerOrJoark(BrevStatusVO brevStatus) throws BrevTechnicalException, BrevFunctionalException;

	void ping();

	void lagreDokument(BrevVO brevVo, BrevStatusVO brevStatusVO, SystemType systemtype) throws BrevException;

	void avbrytDokument(BrevStatusVO brevStatus) throws BrevException;
}
