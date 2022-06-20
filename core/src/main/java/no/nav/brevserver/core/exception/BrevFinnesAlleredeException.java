package no.nav.brevserver.core.exception;

public class BrevFinnesAlleredeException extends BrevFunctionalException {

	public BrevFinnesAlleredeException(int i, String msg) {
		super(i, msg);
	}

	public BrevFinnesAlleredeException(int i, String msg, Exception e) {
		super(i, msg, e);
	}

	public BrevFinnesAlleredeException(String s) {
		super(s);
	}

	public BrevFinnesAlleredeException(String s, Exception e) {
		super(s, e);
	}
}
