package no.nav.brevserver.service;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;

public interface BrevtilgangService {

	boolean sjekkTilgang(String systemId, String brevreferanse, String token) throws BrevTechnicalException;
	boolean sjekkSystemTilgang(String systemId, String passord) throws BrevTechnicalException;
	boolean lagreTilgang(String systemID, String brevreferanse, String token) throws BrevTechnicalException;
}
