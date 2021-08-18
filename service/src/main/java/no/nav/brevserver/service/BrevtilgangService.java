package no.nav.brevserver.service;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.SysTilgangVO;

public interface BrevtilgangService {

	boolean sjekkTilgang(String systemId, String brevreferanse, String token) throws BrevTechnicalException;
	boolean sjekkSystemTilgang(String systemId, String passord) throws BrevTechnicalException;
	boolean lagreTilgang(String systemID, String brevreferanse, String token) throws BrevTechnicalException;
	SysTilgangVO hentTilgangMedCache(String systemId) throws BrevTechnicalException;
	SysTilgangVO hentTilgangUtenCache(String systemId) throws BrevTechnicalException;
}
