package no.nav.brevserver.service.utility;

import no.nav.brevserver.core.constants.KnappStatus;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author Rune Røren, Accenture
 * @version $Revision: 2145 $ $Author: t133126 $ $Date: 2013-07-23 12:06:48 +0200 (ti, 23 jul 2013) $
 */
@Component
public class KnappStatusUtil {

	private final Environment environment;

	public KnappStatusUtil(Environment environment){
		this.environment = environment;
	}

	private static final String PESYS_MAL_PREFIX = "PE_";

	public KnappStatus getKnappStatus(String mal) {
		int result = getInt(environment.getProperty("BrevserverServiceBean.knappstatus.default"), KnappStatus.getDefaultValue());
		if (mal != null) {
			if (mal.trim().startsWith(PESYS_MAL_PREFIX)) {
				result = KnappStatus.getAllActiveValue();
			}
			else {
				result = getInt(environment.getProperty("BrevserverServiceBean.knappstatus." + mal.trim()), result);
			}
		}
		return new KnappStatus(result);
	}

	public int getInt(String value, int defaultInt) {
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return defaultInt;
			}
		} else {
			return defaultInt;
		}
	}

	public KnappStatus getDefaultKnappStatus() {
		return getKnappStatus(null);
	}

}
