package no.nav.brevserver.server.common.utility;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.log.Log;

/**
 * Klasse for å måle ytelsen/responstiden på metoder
 * 
 * @author Rune R�ren, Accenture
 * @version $Revision: 2148 $ $Author: t133126 $ $Date: 2013-07-23 14:24:36
 *          +0200 (ti, 23 jul 2013) $
 */
public final class PerformanceLogger {

	private static boolean shallIMeasure = ConfigManager.getInstance().getBool(
			ConfigManager.PERF_MAAL_YTELSE, false);
	private static int treshold = ConfigManager.getInstance().getInt(
			ConfigManager.PERF_YTELSE_SKRANKE, 10000);

	private static Log log = new Log(PerformanceLogger.class);

	private String callerName = null;
	private long time = 0;

	/**
	 * Starter en m�ling
	 * 
	 * @param callerName
	 *            A name identifying the caller, used for logging
	 */
	public PerformanceLogger(String callerName) {
		this.callerName = callerName;
		if (shallIMeasure) {
			this.time = System.currentTimeMillis();
		}
	}

	/**
	 * Avslutter en m�ling Logger en WARN hvis m�lingen viser at operasjonen tok
	 * over en viss tid (ConfigManager.PERF_YTELSE_SKRANKE)
	 * 
	 */
	public void stop() {
		if (shallIMeasure) {
			long diff = System.currentTimeMillis() - time;
			if (diff >= treshold) {
				String msg = "Operasjonen tok " + Long.toString(diff) + "ms (grenseverdi: " + treshold + "ms)";
				log.warning(callerName, msg);
			}
		}
	}
}
