package no.nav.brevserver.server.common.log;

import no.nav.brevserver.server.common.utility.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rune Røren, Accenture
 */
public class LogCache {

	private static final int LOG_MESSAGES_MAX = 100;
	private static final boolean LOG_MESSAGES = true;
	private static final boolean LOG_ASCENDING = true;

	private static LinkedList<LogCacheInfo> lastErrorMessages = new LinkedList<LogCacheInfo>();
	private static LinkedList<LogCacheInfo> lastSuccessMessages = new LinkedList<LogCacheInfo>();

	private static LogCacheInfo startupMsg = null;

	private static final String[] IMPORTANCE = {"DEBUG", "INFO ", "WARN ", "ERROR", "FATAL", "INFO "};

	/**
	 * Metode for å hente ut de siste logg-meldingene (fra minnet).
	 *
	 * @return de siste logg-meldingene (fra minnet).
	 */
	public static List<String> getLastErrorMessages() {
		return getLastMessages(lastErrorMessages);
	}

	public static List<String> getLastSuccessMessages() {
		return getLastMessages(lastSuccessMessages);
	}

	private static List<String> getLastMessages(List<LogCacheInfo> l) {
		ArrayList<String> result = new ArrayList<String>();

		result.add(convert2String(getStartupMsg()));

		for (int i = 0; i < l.size(); i++) {
			LogCacheInfo logMessage = (LogCacheInfo) l.get(i);
			result.add(convert2String(logMessage));
		}

		return result;

	}

	private static String convert2String(LogCacheInfo x) {
		Date time = new Date(x.timestamp);

		return DateUtil.createDateTimeFormat().format(time) + " " + IMPORTANCE[x.importance - 1] + " " + x.msg;
	}

	/**
	 * Metode for å legge en logg-melding inn i minnet
	 *
	 * @param msg        Melding
	 * @param importance Viktighet
	 */
	public static void addMessage(String msg, int importance) {
		createStartupMsg();

		if (!LOG_MESSAGES) {
			return;
		}

		if (importance == Log.WARNING || importance == Log.FATAL || importance == Log.ERROR) {
			addMessage(lastErrorMessages, msg, importance);

		} else if (importance == Log.INFO) {
			addMessage(lastSuccessMessages, msg, importance);
		}
	}

	private static void addMessage(List<LogCacheInfo> l, String msg, int importance) {
		LogCacheInfo logMessage = new LogCacheInfo();
		logMessage.msg = msg;
		logMessage.timestamp = System.currentTimeMillis();
		logMessage.importance = importance;

		while (l.size() > LOG_MESSAGES_MAX) {
			l.remove(0);
		}

		if (LOG_ASCENDING) {
			l.add(logMessage);
		} else {
			l.add(0, logMessage);
		}

	}

	private static void createStartupMsg() {
		if (startupMsg != null) {
			return;
		}

		startupMsg = new LogCacheInfo();
		startupMsg.msg = "Brevserver startet";
		startupMsg.timestamp = System.currentTimeMillis();
		startupMsg.importance = Log.INFO;
	}

	private static LogCacheInfo getStartupMsg() {
		createStartupMsg();

		return startupMsg;
	}
}
