package no.nav.brevserver.core.exception;

public class BrevserverTechnicalException extends RuntimeException {
	public BrevserverTechnicalException(String message) {
		super(message);
	}

	public BrevserverTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
