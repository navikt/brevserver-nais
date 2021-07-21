package no.nav.brevserver.dokarkiv.exception;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public abstract class AbstractDokdistfordelingFunctionalException extends RuntimeException {

	public AbstractDokdistfordelingFunctionalException(String message) {
		super(message);
	}

	public AbstractDokdistfordelingFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
