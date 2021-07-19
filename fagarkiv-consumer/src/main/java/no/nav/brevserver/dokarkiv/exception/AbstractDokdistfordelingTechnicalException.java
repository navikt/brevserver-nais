package no.nav.brevserver.dokarkiv.exception;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public abstract class AbstractDokdistfordelingTechnicalException extends RuntimeException {

	public AbstractDokdistfordelingTechnicalException(String message) {
		super(message);
	}

	public AbstractDokdistfordelingTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
