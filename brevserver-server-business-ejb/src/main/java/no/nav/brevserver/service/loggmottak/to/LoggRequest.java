package no.nav.brevserver.service.loggmottak.to;


/**
 * Domain request object for the logg operation on the service layer
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class LoggRequest {

	private String systemId;
	private String brevreferanse;
	private String infotrygdId;
	private String klientVersion;
	private String brukerId;
	private int severity;
	private String message;
	private LoggedException exception;

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getBrevreferanse() {
		return brevreferanse;
	}

	public void setBrevreferanse(String brevreferanse) {
		this.brevreferanse = brevreferanse;
	}

	public String getInfotrygdId() {
		return infotrygdId;
	}

	public void setInfotrygdId(String infotrygdId) {
		this.infotrygdId = infotrygdId;
	}

	public String getKlientVersion() {
		return klientVersion;
	}

	public void setKlientVersion(String klientVersion) {
		this.klientVersion = klientVersion;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LoggedException getException() {
		return exception;
	}

	public void setException(LoggedException exception) {
		this.exception = exception;
	}

	public String getBrukerId() {
		return brukerId;
	}

	public void setBrukerId(String brukerId) {
		this.brukerId = brukerId;
	}
}