package no.nav.brevserver.service;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;

public interface BrevstatusService {
	BrevStatusVO hentBrevStatus(String brevreferanse, String systemId) throws BrevTechnicalException;

	BrevStatusVO lagreBrevStatus(BrevStatusVO brevStatus) throws BrevTechnicalException;
}
