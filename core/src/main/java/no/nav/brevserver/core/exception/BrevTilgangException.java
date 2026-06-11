package no.nav.brevserver.core.exception;

public class BrevTilgangException extends BrevserverFunctionalException {

	public BrevTilgangException(String journalpostId, String message) {
		super("Ingen tilgang til brev med journalpostId " + journalpostId + ". " + message);
	}
}
