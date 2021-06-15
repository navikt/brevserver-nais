package no.nav.brevserver.service;

import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.to.LagreDokumentRequest;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;

public interface BrevlagerService {

	BrevVO getBrev(String systemID, String brevReferanse) throws BrevTechnicalException;

	BrevVO hentDokumentFromBrevlagerOrJoark(BrevStatusVO brevStatus) throws BrevTechnicalException, BrevFunctionalException;

	void ping();

	void lagreDokument(LagreDokumentRequest map) throws BrevException;
}
