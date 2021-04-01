package no.nav.brevserver.service.brevlager.beans;

import no.nav.brevserver.builder.BrevBuilder;
import no.nav.brevserver.builder.BrevStatusBuilder;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.jndi.JndiHelper;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.service.AbstractDatabaseTest;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static no.nav.brevserver.builder.BrevBuilder.getBrevBuilder;
import static no.nav.brevserver.builder.BrevStatusBuilder.getBrevStatusBuilder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Unit tests for BrevlagerServiceBean
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BrevserverServiceFactory.class})
public class BrevlagerServiceBeanTest extends AbstractDatabaseTest {

	private static final String BLANK = "";
	private static final String NO_DB2_OPTIMIZATION = BLANK;

	private static final String SYSTEM_ID = "PE00";
	private static final String BREVREFERANSE = "10000000000";
	private static final String TOKEN = "TOKEN_123";
	private static final String BRUKERID = "b111111";
	private static final byte[] BREVDATA = "Hest er best".getBytes();
	private static final byte[] BREVDATA2 = "Hest er best ingen protest".getBytes();

	private static final String RETURKOE = "ReturKoe";
	private static final String BREVMAL = "NAV-01-02-03";
	private static final String STATUS = "FERDIG";
	private static final String FORMAT = FilType.PDF.getJoarkCode();
	private static final String SKRIVERTYPE = "Blekk";
	private static final String SKRIVER = "Canon";
	private static final String ARKIVER = "Ja";
	private static final String SKUFF = "0";

	@Mock
	private BrevserverService brevserverServiceMock;

	private BrevlagerServiceBean brevlagerServiceBean;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		setupBrevserverServiceMock();
		brevlagerServiceBean = new BrevlagerServiceBean();
		brevlagerServiceBean.setDb2SingleRowOptimization(NO_DB2_OPTIMIZATION);
	}

	@Test
	@PrepareForTest({BrevserverServiceFactory.class, JndiHelper.class})
	public void shouldThrowExceptionForFailedQueryInGetBrev() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();

		brevlagerServiceBean.getBrev(SYSTEM_ID, BREVREFERANSE);
	}

	@Test
	@PrepareForTest({BrevserverServiceFactory.class, JndiHelper.class})
	public void shouldThrowExceptionForFailedQueryInLagreBrev() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();

		brevlagerServiceBean.lagreBrev(defaultBrev().build(), new BrevStatusVO());
	}

	@Test
	@PrepareForTest({BrevserverServiceFactory.class, JndiHelper.class})
	public void shouldThrowExceptionForFailedQueryInFerdigstillBrev() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();

		brevlagerServiceBean.ferdigstillBrev(new BrevStatusVO(), defaultBrev().build(), defaultBrev().build());
	}

	@Test
	public void shouldLagreNyttBrevAndVerifyLagret() throws Exception {
		BrevStatusVO brevStatus = defaultBrevStatus().build();
		BrevVO brev = defaultBrev().build();
		when(brevserverServiceMock.lagreBrevStatus(eq(brevStatus), any(Connection.class))).thenReturn(null);

		BrevStatusVO oldBrevStatus = brevlagerServiceBean.lagreBrev(brev, brevStatus);

		verify(brevserverServiceMock).lagreBrevStatus(eq(brevStatus), any(Connection.class));
		BrevVO persistedBrev = brevlagerServiceBean.getBrev(SYSTEM_ID, BREVREFERANSE);

		assertThat(oldBrevStatus, nullValue());
		assertThat(persistedBrev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(persistedBrev.getSystemID(), is(SYSTEM_ID));
		assertThat(persistedBrev.getContentType(), is(FilType.RTF.getContentType()));
		assertThat(persistedBrev.getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_KLADD));
		assertThat(persistedBrev.getBrukerID(), is(BRUKERID));
		assertThat(persistedBrev.getBrevdata(), is(BREVDATA));
	}

	@Test
	public void shouldOppdatereEksisterendeBrevAndVerifyOppdatert() throws Exception {
		brevlagerServiceBean.lagreBrev(defaultBrev().build(), new BrevStatusVO());
		BrevStatusVO initialBrevStatus = defaultBrevStatus().build();
		BrevVO updatedBrev = defaultBrev().contentType(FilType.PDF.getContentType()).brevdata(BREVDATA2).build();
		when(brevserverServiceMock.lagreBrevStatus(any(BrevStatusVO.class), any(Connection.class))).thenReturn(
				initialBrevStatus);

		BrevStatusVO oldBrevStatus = brevlagerServiceBean.lagreBrev(updatedBrev, new BrevStatusVO());

		assertThat(oldBrevStatus, is(initialBrevStatus));

		BrevVO persistedBrev = brevlagerServiceBean.getBrev(SYSTEM_ID, BREVREFERANSE);
		assertThat(persistedBrev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(persistedBrev.getSystemID(), is(SYSTEM_ID));
		assertThat(persistedBrev.getContentType(), is(FilType.PDF.getContentType()));
		assertThat(persistedBrev.getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_KLADD));
		assertThat(persistedBrev.getBrukerID(), is(BRUKERID));
		assertThat(persistedBrev.getBrevdata(), is(BREVDATA2));
	}

	@Test
	public void shouldThrowExceptionIfBrevStatusIsFerdig() throws Exception {
		thrown.expect(BrevTechnicalException.class);
		thrown.expectMessage("Brevet har status = 'FERDIG' og kan ikke endres");

		brevlagerServiceBean.lagreBrev(defaultBrev().lagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG).build(), new BrevStatusVO());
		brevlagerServiceBean.lagreBrev(defaultBrev().build(), new BrevStatusVO());
	}

	@Test
	public void shouldFerdigstilleNyttBrevAndVerifyLagret() throws Exception {
		BrevStatusVO brevStatus = defaultBrevStatus().build();
		BrevVO redBrev = defaultBrev().contentType(FilType.RTF.getContentType()).build();
		BrevVO pdfBrev = defaultBrev().lagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG).contentType(FilType.PDF.getContentType()).build();

		brevlagerServiceBean.ferdigstillBrev(brevStatus, redBrev, pdfBrev);

		verify(brevserverServiceMock).lagreBrevStatus(eq(brevStatus), any(Connection.class));

		BrevVO persistedBrev = brevlagerServiceBean.getBrev(SYSTEM_ID, BREVREFERANSE);

		assertThat(persistedBrev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(persistedBrev.getSystemID(), is(SYSTEM_ID));
		assertThat(persistedBrev.getContentType(), is(FilType.PDF.getContentType()));
		assertThat(persistedBrev.getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_FERDIG));
		assertThat(persistedBrev.getBrukerID(), is(BRUKERID));
		assertThat(persistedBrev.getBrevdata(), is(BREVDATA));
	}

	@Test
	public void shouldFerdigstilleEksisterendeBrevAndVerifyLagret() throws Exception {
		BrevVO redBrev = defaultBrev().contentType(FilType.RTF.getContentType()).build();
		BrevVO pdfBrev = defaultBrev().lagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG)
				.contentType(FilType.PDF.getContentType()).brevdata(BREVDATA2).build();

		brevlagerServiceBean.lagreBrev(redBrev, new BrevStatusVO());
		brevlagerServiceBean.ferdigstillBrev(new BrevStatusVO(), redBrev, pdfBrev);

		BrevVO persistedBrev = brevlagerServiceBean.getBrev(SYSTEM_ID, BREVREFERANSE);

		assertThat(persistedBrev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(persistedBrev.getSystemID(), is(SYSTEM_ID));
		assertThat(persistedBrev.getContentType(), is(FilType.PDF.getContentType()));
		assertThat(persistedBrev.getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_FERDIG));
		assertThat(persistedBrev.getBrukerID(), is(BRUKERID));
		assertThat(persistedBrev.getBrevdata(), is(BREVDATA2));
	}

	@Test
	public void shouldHandleDocx() throws Exception {
		BrevVO redBrev = defaultBrev().contentType(FilType.DOCX.getContentType()).build();
		BrevVO pdfBrev = defaultBrev().lagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG)
				.contentType(FilType.PDF.getContentType()).brevdata(BREVDATA2).build();

		brevlagerServiceBean.lagreBrev(redBrev, new BrevStatusVO());
		BrevVO persistedBrev = brevlagerServiceBean.getBrev(SYSTEM_ID, BREVREFERANSE);
		assertThat(persistedBrev.getContentType(), is(FilType.DOCX.getContentType()));

		brevlagerServiceBean.ferdigstillBrev(new BrevStatusVO(), redBrev, pdfBrev);
		persistedBrev = brevlagerServiceBean.getBrev(SYSTEM_ID, BREVREFERANSE);
		assertThat(persistedBrev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(persistedBrev.getSystemID(), is(SYSTEM_ID));
		assertThat(persistedBrev.getContentType(), is(FilType.PDF.getContentType()));
		assertThat(persistedBrev.getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_FERDIG));
		assertThat(persistedBrev.getBrukerID(), is(BRUKERID));
		assertThat(persistedBrev.getBrevdata(), is(BREVDATA2));
	}

	@Test
	public void shouldPingBrevlager() {
		brevlagerServiceBean.ping();
	}

	private BrevBuilder defaultBrev() {
		return getBrevBuilder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID).contentType(FilType.RTF.getContentType())
				.lagerStatus(Konstanter.BREVLAGER_STATUS_KLADD).brukerID(BRUKERID).brevdata(BREVDATA);
	}

	private BrevStatusBuilder defaultBrevStatus() {
		return getBrevStatusBuilder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID).token(TOKEN).returKoe(RETURKOE)
				.bestillerBrukerID(BRUKERID).brevmal(BREVMAL).status(STATUS).format(FORMAT).skrivertype(SKRIVERTYPE)
				.skriver(SKRIVER).arkiver(ARKIVER).skuff(SKUFF);
	}

	private void setupBrevserverServiceMock() {
		mockStatic(BrevserverServiceFactory.class);
		BrevserverServiceFactory brevserverServiceFactoryMock = mock(BrevserverServiceFactory.class);
		when(BrevserverServiceFactory.getInstance()).thenReturn(brevserverServiceFactoryMock);
		when(brevserverServiceFactoryMock.createBrevserverService()).thenReturn(brevserverServiceMock);
	}

	private void throwExceptionWhenQueryIsExecuted() throws Exception {
		mockStatic(JndiHelper.class);
		JndiHelper jndiHelperMock = mock(JndiHelper.class);
		DataSource dataSourceMock = mock(DataSource.class);
		Connection connectionMock = mock(Connection.class);
		PreparedStatement statementMock = mock(PreparedStatement.class);

		when(JndiHelper.getInstance()).thenReturn(jndiHelperMock);
		when(jndiHelperMock.lookup(DataSource.class, ConfigManager.DATABASE_JNDI)).thenReturn(dataSourceMock);
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