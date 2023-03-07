package no.nav.brevserver.service.utility;

import no.nav.brevserver.core.constants.KnappStatus;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class KnappStatusUtil {

	private final Environment environment;

	public KnappStatusUtil(Environment environment){
		this.environment = environment;
	}

	private static final String PESYS_MAL_PREFIX = "PE_";
	private static final String BIDRAG_SYSTEM_ID = "BI12";

	public KnappStatus getKnappStatus(String mal, String systemId) {
		int result = getInt(environment.getProperty("BrevserverServiceBean.knappstatus.default"), KnappStatus.getDefaultValue());
		if (mal != null) {
			if (mal.trim().startsWith(PESYS_MAL_PREFIX)) {
				result = KnappStatus.getAllActiveValue();
			} else if (Objects.equals(systemId, BIDRAG_SYSTEM_ID)) {
				result = KnappStatus.getAllExceptUtskriftValue();
			} else {
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
		return getKnappStatus(null, null);
	}

}
