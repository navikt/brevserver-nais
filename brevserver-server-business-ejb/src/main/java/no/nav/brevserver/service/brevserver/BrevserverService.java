package no.nav.brevserver.service.brevserver;

import java.sql.Connection;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.SysTilgangVO;

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

	BrevStatusVO hentBrevStatus(String systemID, String brevReferanse)
			throws BrevTechnicalException;

	BrevStatusVO lagreBrevStatus(BrevStatusVO brevStatus, Connection con)
			throws BrevTechnicalException;

	BrevStatusVO lagreBrevStatus(BrevStatusVO brevStatus)
			throws BrevTechnicalException;

	SysTilgangVO hentTilgang(String systemid, boolean useCache)
			throws BrevTechnicalException;
}
