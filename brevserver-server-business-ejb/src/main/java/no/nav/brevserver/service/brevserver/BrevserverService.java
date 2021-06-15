package no.nav.brevserver.service.brevserver;

import java.sql.Connection;

import no.nav.brevserver.core.domain.entities.BrevSystemTilgang;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;

/**
 * BrevserverService interface. Se implementasjonen for detaljer.
 */
public interface BrevserverService {

	boolean lagreTilgang(String systemID, String brevReferanse, String token)
			throws BrevTechnicalException;

	boolean sjekkTilgang(String systemID, String brevReferanse, String token)
			throws BrevTechnicalException;

	boolean sjekkSystemTilgang(String systemID, String passord)
			throws BrevTechnicalException;

	Brevstatus hentBrevStatus(String systemID, String brevReferanse)
			throws BrevTechnicalException;

	Brevstatus lagreBrevStatus(Brevstatus brevStatus, String token)
			throws BrevTechnicalException;

	BrevSystemTilgang hentTilgang(String systemid, boolean useCache)
			throws BrevTechnicalException;
}
