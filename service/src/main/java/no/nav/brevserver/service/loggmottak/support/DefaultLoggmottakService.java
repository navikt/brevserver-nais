package no.nav.brevserver.service.loggmottak.support;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.service.loggmottak.Log;
import no.nav.brevserver.service.loggmottak.LoggmottakService;
import no.nav.brevserver.service.loggmottak.to.LoggRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import static no.nav.brevserver.core.constants.MDCConstants.BREVKLIENT_VERSJON;
import static no.nav.brevserver.core.constants.MDCConstants.BREVREFERANSE_KEY;
import static no.nav.brevserver.core.constants.MDCConstants.INFOTRYGD_ID;
import static no.nav.brevserver.core.constants.MDCConstants.SYSTEMID_KEY;
import static no.nav.brevserver.core.constants.MDCConstants.USER_ID;

/**
 * Default implementation of LoggmottakService
 * Uses the custom brevserver Log wrapper
 */
@Service
@Slf4j
@NoArgsConstructor
public class DefaultLoggmottakService implements LoggmottakService {

	static final String UNKNOWN_SEVERITY_MESSAGE = "Mottok loggrequest med ukjent severity";

	@Override
	public void logg(LoggRequest loggRequest) {
		MDC.put(SYSTEMID_KEY, loggRequest.getSystemId());
		// saksbehandler
		MDC.put(USER_ID, loggRequest.getBrukerId());
		MDC.put(BREVREFERANSE_KEY, loggRequest.getBrevreferanse());
		if (loggRequest.getInfotrygdId() != null) {
			MDC.put(INFOTRYGD_ID, loggRequest.getInfotrygdId());
		}
		MDC.put(BREVKLIENT_VERSJON, loggRequest.getKlientVersion());

		String logMessage = String.format(
				"Melding: %s. Exception: %s",
				loggRequest.getMessage(),
				loggRequest.getException());

		switch (loggRequest.getSeverity()) {
			case Log.FATAL -> log.error("Loggmottak - Kritisk uhåndert teknisk feil i brevklient. {}", logMessage);
			case Log.ERROR -> log.error("Loggmottak - Uhåndtert teknisk feil i brevklient. {}", logMessage);
			case Log.WARNING -> log.warn("Loggmottak - Funksjonell feil i brevklient. {}", logMessage);
			case Log.INFO -> log.info("Loggmottak - Brevklient aktivitet. {}", logMessage);
			default -> log.error(UNKNOWN_SEVERITY_MESSAGE + ": " + loggRequest.getSeverity() + "."
					+ " Mottatt loggrequest: " + logMessage);
		}
	}
}
