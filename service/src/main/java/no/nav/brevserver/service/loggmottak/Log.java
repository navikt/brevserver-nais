package no.nav.brevserver.service.loggmottak;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lett innpakning av Log4J
 */
public class Log {
	public static final int DEBUG = 1;
	public static final int INFO = 2;
	public static final int WARNING = 3;
	public static final int ERROR = 4;
	public static final int FATAL = 5;

	private Logger log;

	public Log(Class<?> clazz) {
		log = LoggerFactory.getLogger(clazz);
	}

	public void info(String who, String message) {
		write(who, message, INFO);
	}

	public void debug(String who, String message) {
		write(who, message, DEBUG);
	}

	public void debug(String who, String message, Exception e) {
		write(who, message, DEBUG, e);
	}

	public void error(String who, String message) {
		write(who, message, ERROR);
	}

	public void error(String who, String message, Exception e) {
		write(who, message, ERROR, e);
	}

	public void warning(String who, String message) {
		write(who, message, WARNING);
	}

	public void warning(String who, String message, Exception e) {
		write(who, message, WARNING, e);
	}

	public void fatal(String who, String message) {
		write(who, message, FATAL);
	}

	public void fatal(String who, String message, Exception e) {
		write(who, message, FATAL, e);
	}

	public void write(String who, String message, int importance) {
		write(who, message, importance, null);
	}

	public void write(String who, String message, int importance, Exception e) {

		if (log == null) {
			log = LoggerFactory.getLogger(getClass());
		}

		String msg = who;

		if (message != null) {
			msg += " - " + message;

			if (importance == INFO) {
				log.info(msg, e);
			} else if (importance == DEBUG) {
				log.debug(msg, e);
			} else if (importance == WARNING) {
				log.warn(msg, e);
			} else {
				log.error(msg, e);
			}

			try {
				//LogCache.addMessage(msg, importance);
			} catch (Exception logCacheException) {
				log.error("Klarte ikke legge logginnslag i LogCache.", logCacheException);
			}
		}
	}

	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}
}
