package no.nav.brevserver.core.utils;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SanitizeLoggingUtil {

	private static final Pattern SANITIZE_CHAR_REGEX = Pattern.compile("[^a-zA-Z0-9]");

	public static String sanitizeInputString(String input) {
		if (isBlank(input)) {
			return null;
		}
		return abbreviate(SANITIZE_CHAR_REGEX.matcher(input).replaceAll("_"), 400);
	}

}
