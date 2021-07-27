package no.nav.brevserver.service;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;

public interface BrevstatusService {
	Brevstatus hentBrevStatus(String systemId, String brevreferanse) throws BrevTechnicalException;

	Brevstatus lagreBrevStatus(Brevstatus brevStatus, String token) throws BrevTechnicalException;

	BrevStatusVO lagreBrevStatus(BrevStatusVO brevStatus)
			throws BrevTechnicalException;
}
