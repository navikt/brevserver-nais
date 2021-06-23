//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ejb;

import java.io.PrintStream;
import java.io.PrintWriter;

public class EJBException extends RuntimeException {
	private Exception causeException = null;

	public EJBException() {
	}

	public EJBException(String message) {
		super(message);
	}

	public EJBException(Exception ex) {
		this.causeException = ex;
	}

	public EJBException(String message, Exception ex) {
		super(message);
		this.causeException = ex;
	}

	public Exception getCausedByException() {
		return this.causeException;
	}

	public String getMessage() {
		String msg = super.getMessage();
		if (this.causeException == null) {
			return msg;
		} else {
			return msg == null ? "nested exception is: " + this.causeException.toString() : msg + "; nested exception is: " + this.causeException.toString();
		}
	}

	public void printStackTrace(PrintStream ps) {
		if (this.causeException == null) {
			super.printStackTrace(ps);
		} else {
			synchronized(ps) {
				ps.println(this);
				this.causeException.printStackTrace(ps);
				super.printStackTrace(ps);
			}
		}

	}

	public void printStackTrace() {
		this.printStackTrace(System.err);
	}

	public void printStackTrace(PrintWriter pw) {
		if (this.causeException == null) {
			super.printStackTrace(pw);
		} else {
			synchronized(pw) {
				pw.println(this);
				this.causeException.printStackTrace(pw);
				super.printStackTrace(pw);
			}
		}

	}
}
