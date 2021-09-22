package no.nav.brevserver.service.loggmottak.support;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.service.loggmottak.Log;
import no.nav.brevserver.service.loggmottak.LoggmottakService;
import no.nav.brevserver.service.loggmottak.to.LoggRequest;
import org.springframework.stereotype.Service;

/**
 * Default implementation of LoggmottakService
 * Uses the custom brevserver Log wrapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Service
@Slf4j
@NoArgsConstructor
public class DefaultLoggmottakService implements LoggmottakService {

	static final String UNKNOWN_SEVERITY_MESSAGE = "Mottok loggrequest med ukjent severity";

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
				log.error("Fatal error! " + methSig, logMessage);
				break;
			case Log.ERROR:
				log.error(methSig, logMessage);
				break;
			case Log.WARNING:
				log.warn(methSig, logMessage);
				break;
			case Log.INFO:
				log.info(methSig, logMessage);
				break;
			default:
				log.error(methSig, UNKNOWN_SEVERITY_MESSAGE + ": " + loggRequest.getSeverity() + "."
						+ " Mottatt loggrequest: " + logMessage);
		}
	}
}
