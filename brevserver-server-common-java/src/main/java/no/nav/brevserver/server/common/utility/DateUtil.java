package no.nav.brevserver.server.common.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Date util class
 *
 * @author Rune Røren, Accenture
 */
public class DateUtil {
	/**
	 * Creates a date and time format.
	 *
	 * @return a date and time format.
	 */
	public static DateFormat createDateTimeFormat() {
		final SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getInstance();
		dateFormat.setLenient(false);
		dateFormat.applyLocalizedPattern("dd.MM.yyyy HH:mm:ss");

		return dateFormat;
	}
}
