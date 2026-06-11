package no.nav.brevserver.core.exception;

public class BrevFinnesIkkeException extends BrevserverFunctionalException {

	public BrevFinnesIkkeException(String journalpostId) {
		super("Finner ikke brev med journalpostId " + journalpostId);
	}
}
