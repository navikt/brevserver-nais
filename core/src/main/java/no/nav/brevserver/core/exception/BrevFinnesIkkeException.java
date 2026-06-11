package no.nav.brevserver.core.exception;

public class BrevFinnesIkkeException extends BrevserverFunctionalException {

	public BrevFinnesIkkeException(String brevreferanse) {
		super("Finner ikke brev med brevreferanse " + brevreferanse);
	}
}
