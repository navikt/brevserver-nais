package no.nav.brevserver.config;

import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.FilType;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * Abstract database testclass. Bootstraps an in-memory H2 database.
 * Performs DDL and cleans up for each test. Also provides convenience methods for database query and updates.
 *
 * @author Joakim Bjornstad, Visma Consulting
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {H2JpaConfig.class})
@Sql(scripts = "classpath:drop-all.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(scripts = {"classpath:create-database.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class AbstractDatabaseTest {

	protected static final String SYSTEM_ID = "BI12";
	protected static final String BREVREFERANSE = "10000000000";
	protected static final String BRUKERID = "b111111";
	protected static final byte[] BREVDATA = "".getBytes();
	protected static final String BLANK = "";
	protected static final String SYSTEM_PASSORD = "Bisys123";
	protected static final String BESTILLER_ID = "b1111";
	protected static final String RETURKOE = "ReturKoe";
	protected static final String BREVMAL = "NAV-01-02-03";
	protected static final String STATUS_FERDIG = "FERDIG";
	protected static final String STATUS_KLADD = "KLADD";
	protected static final String FORMAT = FilType.PDF.getJoarkCode();
	protected static final String SKRIVERTYPE = "Blekk";
	protected static final String SKRIVER = "Canon";
	protected static final String ARKIVER = "Ja";
	protected static final String SKUFF = "0";
	protected static final String TOKEN = "Token";

	public BrevStatusVO createBrevstatus(String systemId, String brevref){
		BrevStatusVO brevstatus = new BrevStatusVO();
		brevstatus.setSystemID(systemId);
		brevstatus.setBrevreferanse(brevref);
		brevstatus.setReturKoe(RETURKOE);
		brevstatus.setBrevmal(BREVMAL);
		brevstatus.setStatus(STATUS_KLADD);
		brevstatus.setFormat(FORMAT);
		brevstatus.setToken(TOKEN);
		brevstatus.setBestillerBrukerID(BRUKERID);
		brevstatus.setSkrivertype(SKRIVERTYPE);
		brevstatus.setSkriver(SKRIVER);
		brevstatus.setArkiver(ARKIVER);
		brevstatus.setSkuff(SKUFF);
		return brevstatus;
	}




}
