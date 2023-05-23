package no.nav.brevserver.core.exception;

public class OperationalException extends RuntimeException {
	public OperationalException(String s, Exception e) {
		super(s, e);
	}
}
