package no.nav.brevserver.service.brevserver.beans;

import no.nav.brevserver.builder.BrevStatusBuilder;
import no.nav.brevserver.server.common.cache.CacheManager;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.jndi.JndiHelper;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.server.common.vo.SysTilgangVO;
import no.nav.brevserver.service.AbstractDatabaseTest;
import no.nav.brevserver.service.TempJndiHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static no.nav.brevserver.builder.BrevStatusBuilder.getBrevStatusBuilder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Unit tests for BrevserverServiceBean
 *
 * @author Joakim Bj�rnstad, Visma Consulting
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CacheManager.class, TempJndiHelper.class})
public class BrevserverServiceBeanTest extends AbstractDatabaseTest {

	private static final String BLANK = "";
	private static final String SYSTEM_ID = "PE00";
	private static final String SYSTEM_PASSORD = "Pensjon123";
	private static final String BREVREFERANSE = "10000000000";
	private static final String BESTILLER_ID = "b1111";
	private static final String RETURKOE = "ReturKoe";
	private static final String BREVMAL = "NAV-01-02-03";
	private static final String STATUS = "FERDIG";
	private static final String FORMAT = FilType.PDF.getJoarkCode();
	private static final String SKRIVERTYPE = "Blekk";
	private static final String SKRIVER = "Canon";
	private static final String ARKIVER = "Ja";
	private static final String SKUFF = "0";
	private static final String TOKEN = "Token";

	private static final String NO_DB2_OPTIMIZATION = BLANK;

	private BrevserverServiceBean brevserverService;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		mockStatic(CacheManager.class);
		brevserverService = new BrevserverServiceBean();
		mockStatic(TempJndiHelper.class);
		DataSource dataSourceMock = jndiDataSource();
		when(TempJndiHelper.jndiDataSource()).thenReturn(dataSourceMock);
		brevserverService.setDb2SingleRowOptimization(NO_DB2_OPTIMIZATION);
	}

	@Test
	public void shouldLagreTilgangAndReturnTrue() throws Exception {
		boolean lagretTilgang = brevserverService.lagreTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);
		assertThat(lagretTilgang, is(true));
	}

	@Test
	public void shouldSjekkeTilgangAndReturnFalseForNoResult() throws Exception {
		boolean lagretTilgang = brevserverService.sjekkTilgang(BLANK, BLANK, BLANK);
		assertThat(lagretTilgang, is(false));
	}

	@Test
	public void shouldSjekkeTilgangAndReturnTrueForLagretTilgang() throws Exception {
		brevserverService.lagreTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);
		boolean lagretTilgang = brevserverService.sjekkTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);
		assertThat(lagretTilgang, is(true));
	}

	@Test
	@PrepareForTest({TempJndiHelper.class, CacheManager.class})
	public void shouldThrowExceptionIfLagreTilgangFailsBecauseOfWrongStatement() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();
		brevserverService.lagreTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);
	}

	@Test
	public void shouldLagreBrevStatusForNyttBrev() throws Exception {
		BrevStatusVO validBrevStatus = defaultBrevStatus().build();
		BrevStatusVO returnedBrevStatus = brevserverService.lagreBrevStatus(validBrevStatus);

		assertThat(returnedBrevStatus, nullValue());
	}

	@Test
	public void shouldLagreBrevStatusForNyttBrevOgLagreTilgangMedToken() throws Exception {
		BrevStatusVO validBrevStatus = defaultBrevStatus().token(TOKEN).build();
		BrevStatusVO returnedBrevStatus = brevserverService.lagreBrevStatus(validBrevStatus);
		boolean lagretTilgang = brevserverService.sjekkTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);

		assertThat(returnedBrevStatus, nullValue());
		assertThat(lagretTilgang, is(true));
	}

	@Test
	public void shouldReturnereGammelBrevStatusForOppdateringAvBrevStatus() throws Exception {
		BrevStatusVO existingBrevStatus = defaultBrevStatus().build();
		brevserverService.lagreBrevStatus(existingBrevStatus);

		BrevStatusVO newBrevStatus = getBrevStatusBuilder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID)
				.returKoe("Returko").bestillerBrukerID("Brannmann").brevmal("NAV1").status("UFERDIG").format(FilType.RTF.getJoarkCode())
				.skrivertype("Laser").skriver("HP").arkiver("Nei").skuff("33").build();

		BrevStatusVO oldBrevStatus = brevserverService.lagreBrevStatus(newBrevStatus);

		assertDefaultBrevStatusValues(oldBrevStatus);
	}

	@Test
	public void shouldOppdatereBrevStatus() throws Exception {
		BrevStatusVO existingBrevStatus = defaultBrevStatus().returKoe("E18").brevmal("NAV1").build();
		brevserverService.lagreBrevStatus(existingBrevStatus);

		BrevStatusVO newBrevStatus = getBrevStatusBuilder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID)
				.returKoe(null).bestillerBrukerID("Brannmann").brevmal(null).status("UFERDIG").format(FilType.RTF.getJoarkCode())
				.skrivertype("Laser").skriver("HP").arkiver("Nei").skuff("33").build();
		brevserverService.lagreBrevStatus(newBrevStatus);

		BrevStatusVO actualBrevStatus = brevserverService.hentBrevStatus(SYSTEM_ID, BREVREFERANSE);

		assertThat(actualBrevStatus.getReturKoe(), is("E18"));
		assertThat(actualBrevStatus.getBestillerBrukerID(), is("Brannmann"));
		assertThat(actualBrevStatus.getBrevmal(), is("NAV1"));
		assertThat(actualBrevStatus.getStatus(), is("UFERDIG"));
		assertThat(actualBrevStatus.getFormat(), is("RTF"));
		assertThat(actualBrevStatus.getSkrivertype(), is("Laser"));
		assertThat(actualBrevStatus.getSkriver(), is("HP"));
		assertThat(actualBrevStatus.getArkiver(), is("Nei"));
		assertThat(actualBrevStatus.getSkuff(), is("33"));
	}

	@Test
	public void shouldOppdatereBrevStatusOgFylleInnManglendeVerdierFraGammelBrevStatus() throws Exception {
		BrevStatusVO existingBrevStatus = defaultBrevStatus().returKoe(null).brevmal(null).build();
		brevserverService.lagreBrevStatus(existingBrevStatus);

		BrevStatusVO newBrevStatus = getBrevStatusBuilder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID)
				.returKoe(RETURKOE).brevmal(BREVMAL).status("PRINTET").build();
		brevserverService.lagreBrevStatus(newBrevStatus);

		BrevStatusVO actualBrevStatus = brevserverService.hentBrevStatus(SYSTEM_ID, BREVREFERANSE);

		assertThat(actualBrevStatus.getReturKoe(), is(RETURKOE));
		assertThat(actualBrevStatus.getBestillerBrukerID(), is(BESTILLER_ID));
		assertThat(actualBrevStatus.getBrevmal(), is(BREVMAL));
		assertThat(actualBrevStatus.getStatus(), is("PRINTET"));
		assertThat(actualBrevStatus.getFormat(), is(FORMAT));
		assertThat(actualBrevStatus.getSkrivertype(), is(SKRIVERTYPE));
		assertThat(actualBrevStatus.getSkriver(), is(SKRIVER));
		assertThat(actualBrevStatus.getArkiver(), is(ARKIVER));
		assertThat(actualBrevStatus.getSkuff(), is(SKUFF));
	}

	@Test
	@PrepareForTest({TempJndiHelper.class, CacheManager.class})
	public void shouldThrowExceptionIfLagreBrevStatusFailsBecauseOfWrongStatement() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();
		brevserverService.lagreBrevStatus(defaultBrevStatus().build());
	}

	@Test
	public void shouldHenteBrevStatus() throws Exception {
		BrevStatusVO existingBrevStatus = defaultBrevStatus().build();
		brevserverService.lagreBrevStatus(existingBrevStatus);

		BrevStatusVO brevStatus = brevserverService.hentBrevStatus(SYSTEM_ID, BREVREFERANSE);

		assertDefaultBrevStatusValues(brevStatus);
	}

	@Test
	public void shouldReturnNullForNonExistingBrevStatus() throws Exception {
		BrevStatusVO brevStatus = brevserverService.hentBrevStatus(SYSTEM_ID, BREVREFERANSE);

		assertThat(brevStatus, nullValue());
	}

	@Test
	@PrepareForTest({TempJndiHelper.class, CacheManager.class})
	public void shouldThrowExceptionIfHentBrevStatusFailsBecauseOfWrongStatement() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();
		brevserverService.hentBrevStatus(SYSTEM_ID, BREVREFERANSE);
	}

	@Test
	public void shouldThrowExceptionForNotAllowedNullField() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		BrevStatusVO invalidBrevStatus = defaultBrevStatus().brevreferanse(null).systemID(null).build();
		brevserverService.lagreBrevStatus(invalidBrevStatus);
	}

	@Test
	public void shouldReturnTrueForGyldigSystemTilgang() throws Exception {
		createSystemTilgang();

		boolean actual = brevserverService.sjekkSystemTilgang(SYSTEM_ID, SYSTEM_PASSORD);

		assertThat(actual, is(true));
	}

	@Test
	public void shouldReturnTrueForGyldigCachedSystemTilgang() throws Exception {
		when(CacheManager.getObject("BrevserverServiceBean.sjekkSystemTilgang(" + SYSTEM_ID + ")")).thenReturn(
				SYSTEM_PASSORD);

		boolean actual = brevserverService.sjekkSystemTilgang(SYSTEM_ID, SYSTEM_PASSORD);

		assertThat(actual, is(true));
	}

	@Test
	public void shouldReturnFalseForUgyldigSystemTilgang() throws Exception {
		createSystemTilgang();

		boolean actual = brevserverService.sjekkSystemTilgang("BI00", "HestErBest");

		assertThat(actual, is(false));
	}

	@Test
	@PrepareForTest({TempJndiHelper.class, CacheManager.class})
	public void shouldThrowExceptionIfSjekkSystemtilgangFailsBecauseOfWrongStatement() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();
		brevserverService.sjekkSystemTilgang(SYSTEM_ID, SYSTEM_PASSORD);
	}

	@Test
	public void shouldHenteSystilgang() throws Exception {
		createSystemTilgang();

		SysTilgangVO sysTilgang = brevserverService.hentTilgang(SYSTEM_ID, false);

		assertThat(sysTilgang.getSysId(), is(SYSTEM_ID));
		assertThat(sysTilgang.getPwd(), is(SYSTEM_PASSORD));
	}

	@Test
	public void shouldHenteSystilgangFromCache() throws Exception {
		SysTilgangVO cachedSysTilgang = new SysTilgangVO();
		cachedSysTilgang.setSysId(SYSTEM_ID);
		cachedSysTilgang.setPwd(SYSTEM_PASSORD);
		when(CacheManager.getObject("BrevserverServiceBean.hentTilgang(" + SYSTEM_ID + ")")).thenReturn(
				cachedSysTilgang);

		SysTilgangVO sysTilgang = brevserverService.hentTilgang(SYSTEM_ID, true);

		assertThat(sysTilgang.getSysId(), is(SYSTEM_ID));
		assertThat(sysTilgang.getPwd(), is(SYSTEM_PASSORD));
	}

	@Test
	@PrepareForTest({TempJndiHelper.class, CacheManager.class})
	public void shouldThrowExceptionIfHentTilgangFailsBecauseOfWrongStatement() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();
		brevserverService.hentTilgang(SYSTEM_ID, false);
	}

	@Test
	public void shouldReturnNullforSystilgangSomIkkeEksisterer() throws Exception {
		SysTilgangVO sysTilgang = brevserverService.hentTilgang(SYSTEM_ID, false);

		assertThat(sysTilgang, nullValue());
	}

	private BrevStatusBuilder defaultBrevStatus() {
		return getBrevStatusBuilder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID).returKoe(RETURKOE)
				.bestillerBrukerID(BESTILLER_ID).brevmal(BREVMAL).status(STATUS).format(FORMAT)
				.skrivertype(SKRIVERTYPE).skriver(SKRIVER).arkiver(ARKIVER).skuff(SKUFF);
	}

	private void assertDefaultBrevStatusValues(BrevStatusVO brevStatus) {
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getReturKoe(), is(RETURKOE));
		assertThat(brevStatus.getBestillerBrukerID(), is(BESTILLER_ID));
		assertThat(brevStatus.getBrevmal(), is(BREVMAL));
		assertThat(brevStatus.getStatus(), is(STATUS));
		assertThat(brevStatus.getFormat(), is(FORMAT));
		assertThat(brevStatus.getSkrivertype(), is(SKRIVERTYPE));
		assertThat(brevStatus.getSkriver(), is(SKRIVER));
		assertThat(brevStatus.getArkiver(), is(ARKIVER));
		assertThat(brevStatus.getSkuff(), is(SKUFF));
	}

	private void createSystemTilgang() {
		executeSql("insert into t_brevsystilgang(systemid, systempassord) values('PE00', 'Pensjon123')");
	}

	private void throwExceptionWhenQueryIsExecuted() throws Exception {
		Connection connectionMock = mock(Connection.class);
		PreparedStatement statementMock = mock(PreparedStatement.class);
		mockStatic(TempJndiHelper.class);
		DataSource dataSourceMock = mock(DataSource.class);
		ReflectionTestUtils.setField(brevserverService, "datasource", dataSourceMock);
		when(dataSourceMock.getConnection(any(String.class), any(String.class))).thenReturn(connectionMock);
		when(connectionMock.createStatement()).thenReturn(statementMock);
		when(connectionMock.prepareStatement(any(String.class))).thenReturn(statementMock);
		when(statementMock.execute("SELECT 1 FROM SYSIBM.SYSDUMMY1")).thenReturn(true);
		when(statementMock.executeUpdate()).thenThrow(new SQLException());
		when(statementMock.executeQuery()).thenThrow(new SQLException());
	}

	private void expectExceptionDatabaseNoDatabaseTilgjengelig() {
		thrown.expect(BrevTechnicalException.class);
		thrown.expectMessage("Databasen til brevserveren er ikke tilgjengelig");
	}
}
