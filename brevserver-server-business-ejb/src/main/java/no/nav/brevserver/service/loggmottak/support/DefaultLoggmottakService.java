package no.nav.brevserver.service.loggmottak.support;

import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.service.loggmottak.LoggmottakService;
import no.nav.brevserver.service.loggmottak.to.LoggRequest;

/**
 * Default implementation of LoggmottakService
 * Uses the custom brevserver Log wrapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultLoggmottakService implements LoggmottakService {

	static final String UNKNOWN_SEVERITY_MESSAGE = "Mottok loggrequest med ukjent severity";
	
	private Log log;

	public DefaultLoggmottakService() {
		log = new Log(DefaultLoggmottakService.class);
	}
	
	@Override
	public void logg(LoggRequest loggRequest) {
		String methSig = String.format(
				"logg(%s:%s/%s,%s,v%s)",
				loggRequest.getSystemId(),
				loggRequest.getBrevreferanse(),
				loggRequest.getInfotrygdId(),
				loggRequest.getBrukerId(),
				loggRequest.getKlientVersion());

		String logMessage = String.format(
				"Melding: %s. Exception: %s",
				loggRequest.getMessage(),
				loggRequest.getException());

		switch (loggRequest.getSeverity()) {
			case Log.FATAL:
				log.fatal(methSig, logMessage);
				break;
			case Log.ERROR:
				log.error(methSig, logMessage);
				break;
			case Log.WARNING:
				log.warning(methSig, logMessage);
				break;
			case Log.INFO:
				log.info(methSig, logMessage);
				break;
			default:
				log.error(methSig, UNKNOWN_SEVERITY_MESSAGE + ": " + loggRequest.getSeverity() + "."
						+ " Mottatt loggrequest: " + logMessage);
		}
	}
	
	public void setLog(Log log) {
		this.log = log;
	}
}
 