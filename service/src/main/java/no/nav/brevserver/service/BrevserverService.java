package no.nav.brevserver.service;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;

public interface BrevserverService {
	Brevstatus hentBrevStatus(String systemId, String brevreferanse) throws BrevTechnicalException;
}
