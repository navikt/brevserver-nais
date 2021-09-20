package no.nav.brevserver;

import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;

public abstract class AbstractBrevserviceTest {

	protected static final String BREVREFERANSE = "1";
	protected static final String TOKEN = "123";
	protected static final String SYSTEM_ID = "PE2";
	protected static final String BRUKER_ID = "brukerID";
	protected static final String CONTENT_TYPE_RTF = FilType.RTF.getContentType();
	protected static final String KVITTERINGSKOE = "kvitteringsKoe";
	protected static final String MALPAKKE = "malpakke";
	protected static final byte[] DOKUMENTDATA_RTF = "hello rtf".getBytes();

	protected BrevVO createBrev(String contentType) {
		BrevVO brev = new BrevVO();
		brev.setBrukerID(BRUKER_ID);
		brev.setSystemID(SYSTEM_ID);
		brev.setBrevreferanse(BREVREFERANSE);
		brev.setBrevdata(DOKUMENTDATA_RTF);
		brev.setContentType(contentType);
		return brev;
	}

	protected BrevStatusVO createBrevStatus() {
		BrevStatusVO brevStatus = new BrevStatusVO();
		brevStatus.setBrevreferanse(BREVREFERANSE);
		brevStatus.setSystemID(SYSTEM_ID);
		brevStatus.setToken(TOKEN);
		brevStatus.setBrevmal(MALPAKKE);
		brevStatus.setReturKoe(KVITTERINGSKOE);
		return brevStatus;
	}
}
