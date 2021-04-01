package no.nav.brevserver.server.common.utility;

/**
 * @author Rune Røren, Accenture
 * @version $Revision: 315 $ $Author: rra2920 $ $Date: 2005-08-09 14:26:03 +0200 (ti, 09 aug 2005) $
 */
public class ArgumentValidator {

	public static void isNotNull(Object object) {
		isNotNull("Parameteren er ugyldig (null)", object);
	}

	public static void isNotNull(String errorMsg, Object object) {
		if (object == null) {
			throw new IllegalArgumentException(errorMsg);
		}
	}
}
