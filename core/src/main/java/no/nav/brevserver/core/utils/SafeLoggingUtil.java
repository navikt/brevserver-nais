package no.nav.brevserver.core.utils;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SafeLoggingUtil {
	private static final int STRING_MAXLENGTH = 500;
	private static final Pattern EVERYTHING_EXCEPT_SAFE_CHARS_REGEX = Pattern.compile("[^a-zA-Z0-9]");

	public static String sanitizeUnsafeChar(String input) {
		if (isBlank(input)) {
			return null;
		}
		return abbreviate(EVERYTHING_EXCEPT_SAFE_CHARS_REGEX.matcher(input).replaceAll("_"), STRING_MAXLENGTH);
	}
}
