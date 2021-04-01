package no.nav.brevserver.command;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.MessageVO;

public class XmlLogger {

	private static Log log = new Log(XmlLogger.class);

	/**
	 * Logs the given message's string body
	 *
	 * @param systemType The System type (PE or BI)
	 * @param message The messsage
	 */
	public static void logXml(SystemType systemType, MessageVO message) {
		if (ConfigManager.getInstance().getBool(ConfigManager.XML_LOGGER_ON, false)) {
			log.debug("XmlLogger.logXml(" + systemType + ")", removePasswords(message.getStringBody()));
		}
	}

	/**
	 * Masks secrets from xml
	 *
	 * @param xml The xml as string
	 * @return Masked xml as string
	 */
	static String removePasswords(String xml) {
		if (xml != null) {
			return xml
					.replaceAll("passord=\".*?\"", "passord=\"" + Konstanter.MASKED_PASSWORD + "\"")
					.replaceAll("klientToken=\".*?\"", "klientToken=\"" + Konstanter.MASKED_PASSWORD + "\"");
		}
		return null;
	}

}
