package no.nav.brevserver.server.common.utility;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.KnappStatus;

/**
 * @author Rune Røren, Accenture
 * @version $Revision: 2145 $ $Author: t133126 $ $Date: 2013-07-23 12:06:48 +0200 (ti, 23 jul 2013) $
 */
public class KnappStatusUtil {

	private static final String PESYS_MAL_PREFIX = "PE_";

	public static KnappStatus getKnappStatus(String mal) {
		int result = ConfigManager.getInstance().getInt("BrevserverServiceBean.knappstatus.default",
				KnappStatus.getDefaultValue());
		if (mal != null) {
			if (mal.trim().startsWith(PESYS_MAL_PREFIX)) {
				result = KnappStatus.getAllActiveValue();
			}
			else {
				result = ConfigManager.getInstance().getInt("BrevserverServiceBean.knappstatus." + mal.trim(), result);
			}
		}
		return new KnappStatus(result);
	}

	public static KnappStatus getDefaultKnappStatus() {
		return getKnappStatus(null);
	}

}
