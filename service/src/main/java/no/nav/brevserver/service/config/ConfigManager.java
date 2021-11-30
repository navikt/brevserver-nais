package no.nav.brevserver.service.config;


import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Configuration loader for brevserver
 */
@Slf4j
public final class ConfigManager {
	public static final String DATABASE_JNDI = "jdbc/brev";
	public static final String QCF_JNDI = "jms/qcf/brev";

	public static final String PERF_MAAL_YTELSE = "PerformanceLogger.ytelsesmaaling.paa";
	public static final String PERF_YTELSE_SKRANKE = "PerformanceLogger.ytelsesmaaling.skranke";

	public static final String ARKIVER_HEADER_LENGDE = "ArkiverBrevCommand.headerLengde";
	public static final String ARKIVER_HEADER_LENGDE_TEGN_I_STARTEN = "ArkiverBrevCommand.headerlengde.antTegnIStartAvMelding";

	public static final String DEADLETTER_REPLY_QUEUE_BI = "BIMessageProducer.deadletter.setReplyQueue";
	public static final String DEADLETTER_REPLY_QUEUE_PE = "PEMessageProducer.deadletter.setReplyQueue";

	public static final String ASPOSE_LICENSE_LOCATION = "aspose.license.location";
	public static final String ASPOSE_FONTS_LOCATION = "aspose.fonts.location";

	public static final String BREVSERVER_URL = "brevserverUrl.url";
	public static final String JOARK_JOURNAL_WS_URL = "joarkJournalUrl.url";
	public static final String JOARK_JOURNALBEHANDLING_WS_URL = "joarkJournalBehandlingUrl.url";

	public static final String DATABASE_USERNAME = "BREVSERVER_DS_USERNAME";
	public static final String DATABASE_PASSWORD = "BREVSERVER_DS_PASSWORD";

	public static final String XML_LOGGER_ON = "XmlLogger.On";
	public static final String DATABASE_URL = "BREVSERVER_DS_URL";

	private static ConfigManager instance = null;
	private Properties properties = null;
	private static final String FIL = "brev.properties";

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

	/**
	 * Get a no.nav.brevserver.config value as int. Checks system properties then resource bundle.
	 * Defaults to defaultStr if neither are found.
	 *
	 * @param name       The no.nav.brevserver.config name
	 * @param defaultInt The no.nav.brevserver.config valeu if no property is found
	 * @return The no.nav.brevserver.config value
	 */
	public int getInt(String name, int defaultInt) {
		String configValue = getString(name, null);
		if (configValue != null) {
			try {
				return Integer.parseInt(configValue);
			} catch (NumberFormatException e) {
				return defaultInt;
			}
		} else {
			return defaultInt;
		}
	}

	/**
	 * Get a no.nav.brevserver.config value as boolean. Checks system properties then resource bundle.
	 * Defaults to defaultStr if neither are found.
	 *
	 * @param name        The no.nav.brevserver.config name
	 * @param defaultBool The no.nav.brevserver.config valeu if no property is found
	 * @return The no.nav.brevserver.config value
	 */
	public boolean getBool(String name, boolean defaultBool) {
		String configValue = getString(name, null);
		if (configValue != null) {
			return "true".equalsIgnoreCase(configValue);
		} else {
			return defaultBool;
		}
	}

	public boolean clearConfig() {
		return loadResourceBundle();
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

	public List<String> getAllProperties() {
		List<String> result = new ArrayList<>();
		Enumeration<Object> keys = properties.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = properties.getProperty(key);
			if (DATABASE_PASSWORD.equals(key)) {
				value = "[fjernet]";
			}
			result.add(key + "=" + value);
		}
		Collections.sort(result);
		return result;
	}
}