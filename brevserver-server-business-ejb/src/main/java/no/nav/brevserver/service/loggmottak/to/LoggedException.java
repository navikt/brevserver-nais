package no.nav.brevserver.service.loggmottak.to;

/**
 * Class that can hold data about an exception thrown in brevklient
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class LoggedException {
	private String message;
	private String stacktrace;

	@SuppressWarnings("unused")
	private LoggedException() {
	}

	public LoggedException(String message, String stacktrace) {
		this.message = message;
		this.stacktrace = stacktrace;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStacktrace() {
		return stacktrace;
	}

	public void setStacktrace(String stacktrace) {
		this.stacktrace = stacktrace;
	}

	@Override
	public String toString() {
		return String.format("message: %s.\nStacktrace: %s", message, stacktrace);
	}
}
