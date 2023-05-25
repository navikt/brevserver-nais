package no.nav.brevserver.service;

import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;


/**
 * Abstract database testclass. Bootstraps an in-memory H2 database.
 * Performs DDL and cleans up for each test. Also provides convenience methods for database query and updates.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {H2JpaConfig.class})
@Import(JmsItestConfig.class)
public abstract class AbstractDatabaseTest {

	protected static final String SYSTEM_ID = "BI12";
	protected static final String BREVREFERANSE = "10000000000";
	protected static final String BRUKERID = "b111111";
	protected static final byte[] BREVDATA = "Hest er best".getBytes();
	protected static final String BLANK = "";
	protected static final String SYSTEM_PASSORD = "Pensjon123";
	protected static final String BESTILLER_ID = "b1111";
	protected static final String RETURKOE = "ReturKoe";
	protected static final String BREVMAL = "NAV-01-02-03";
	protected static final String STATUS = "FERDIG";
	protected static final String FORMAT = FilType.PDF.getJoarkCode();
	protected static final String SKRIVERTYPE = "Blekk";
	protected static final String SKRIVER = "Canon";
	protected static final String ARKIVER = "Ja";
	protected static final String SKUFF = "0";
	protected static final String TOKEN = "Token";

	protected BrevVO.BrevVOBuilder defaultBrev() {
		return BrevVO.builder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID).contentType(FilType.RTF.getContentType())
				.lagerStatus(Konstanter.BREVLAGER_STATUS_KLADD).brukerID(BRUKERID).brevdata(BREVDATA);
	}

}
