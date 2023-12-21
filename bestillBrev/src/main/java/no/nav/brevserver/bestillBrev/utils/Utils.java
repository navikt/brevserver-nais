package no.nav.brevserver.bestillBrev.utils;

import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.utils.xmlHandlers.XMLService;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.KvitteringVO;

public class Utils {

	/**
	 * Lager feilmelding basert på feiltype.
	 *
	 * @param feilType
	 * @return Feilmeldings-XML
	 */
	public static String lagFeilmelding(String feilType, BrevStatusVO brevStatusVo) {
		KvitteringVO kvittering = new KvitteringVO();
		kvittering.setSystemID(brevStatusVo.getSystemID());
		kvittering.setBrevreferanse(brevStatusVo.getBrevreferanse());
		kvittering.setFeilkode(feilType);
		brevStatusVo.setStatus(Konstanter.BREVSTATUS_FEIL);

		return XMLService.unmarshal(kvittering, brevStatusVo);
	}
}
