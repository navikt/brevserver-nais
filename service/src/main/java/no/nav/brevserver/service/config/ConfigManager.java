package no.nav.brevserver.service.config;


import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration loader for brevserver
 */
@Slf4j
public final class ConfigManager {
	public static final String ASPOSE_LICENSE_LOCATION = "aspose.license.location";
	public static final String ASPOSE_FONTS_LOCATION = "aspose.fonts.location";

	private static ConfigManager instance = null;
	private Properties properties = null;
	private static final String FIL = "aspose.properties";

	private ConfigManager() {
		loadResourceBundle();
	}

	public static ConfigManager getInstance() {
		if (instance == null) {
			instance = new ConfigManager();
		}
		return instance;
	}

	/**
	 * Get a no.nav.brevserver.config value. Checks system properties then resource bundle.
	 * Defaults to defaultStr if neither are found.
	 *
	 * @param name       The no.nav.brevserver.config name
	 * @param defaultStr The no.nav.brevserver.config valeu if no property is found
	 * @return The no.nav.brevserver.config value
	 */
	public String getString(String name, String defaultStr) {
		String systemProperty = System.getProperty(name);
		if (systemProperty != null) {
			return systemProperty;
		} else {
			return properties.getProperty(name, defaultStr);
		}
	}

	private boolean loadResourceBundle() {
		boolean result = true;
		String methSig = "ConfigManager.loadResourceBundle()";
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(FIL)) {
			properties = new Properties();
			properties.load(is);
			log.info(methSig, "Lastet konfigurasjon");
		} catch (FileNotFoundException e) {
			log.error(methSig, "Fant ikke filen " + FIL, e);
			result = false;
		} catch (IOException e) {
			log.error(methSig, "Feil ved lesing av " + FIL, e);
			result = false;
		}
		return result;
	}

}