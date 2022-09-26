package no.nav.brevserver.service;

import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;

public interface BrevstatusService {
	BrevStatusVO hentBrevStatus(String brevreferanse, String systemId) throws BrevTechnicalException;

	BrevStatusVO lagreBrevStatus(BrevStatusVO brevStatus) throws BrevTechnicalException;
}
