package no.nav.brevserver.server.common.exception;

public class BrevRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 2324375389803110131L;

	public BrevRuntimeException(String msg, BrevException e) {
		super(msg, e);
	}

	public BrevRuntimeException(String msg) {
		super(msg);
	}

	public BrevException getCause() {
		return (BrevException) super.getCause();
	}
}
