package no.nav.brevserver.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.jndi.JndiHelper;
import no.nav.brevserver.server.common.log.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Log.class, ConfigManager.class, JndiHelper.class, SQLService.class })
@SuppressStaticInitializationFor({ "no.nav.brevserver.server.common.jndi.JndiHelper" })
public class SQLServiceTest {

	@Mock
	private ConfigManager configManagerMock;
	@Mock
	private DataSource dataSourceMock;
	@Mock
	private Connection connectionMock;
	@Mock
	private Statement statementMock;

	private SQLService sqlService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockLog();
		mockConfigManager();
		when(dataSourceMock.getConnection()).thenReturn(connectionMock);
		when(connectionMock.createStatement()).thenReturn(statementMock);
		sqlService = new SQLService() {
		};
	}

	@Test
	@PrepareForTest({Log.class, ConfigManager.class})
	public void shouldGetDatabaseInfoFromConfig() {
		verify(configManagerMock).getString(ConfigManager.DATABASE_USERNAME, null);
		verify(configManagerMock).getString(ConfigManager.DATABASE_PASSWORD, null);
	}




	public void shouldGetDatasourceFromJndi() throws BrevTechnicalException {
		try {
			sqlService.createSqlConnection();
		} catch (Exception e) {
		}
		//verify(jndiHelperMock).lookup(DataSource.class, ConfigManager.DATABASE_JNDI);
	}
	
	public void shouldFailIfDataSourceGetConnectionFails() throws Exception {
		when(dataSourceMock.getConnection(null, null)).thenThrow(new SQLException("Testing"));
		try {
			sqlService.createSqlConnection();
			fail("Should not get here!");
		} catch (Exception e) {
			//verify(jndiHelperMock).lookup(DataSource.class, ConfigManager.DATABASE_JNDI);
		}
	}
	
	@Test
	@PrepareForTest({Log.class, ConfigManager.class})
	public void shouldReturnValidConnection() throws Exception {
		when(statementMock.execute(any(String.class))).thenReturn(true);
		assertThat(sqlService.createSqlConnection(), is(connectionMock));
		verify(connectionMock).createStatement();
		verify(connectionMock).setAutoCommit(false);
	}
	
	@Test
	@PrepareForTest({Log.class, ConfigManager.class})
	public void shouldReconnectInvalidConnection() throws Exception {
		when(statementMock.execute(any(String.class))).thenThrow(new SQLException()).thenReturn(false, true);
		sqlService.createSqlConnection();
		verify(dataSourceMock, times(3)).getConnection(null, null);
		verify(connectionMock, times(2)).close();
	}
	
	@Test
	@PrepareForTest({Log.class, ConfigManager.class})
	public void shouldFailIfMaximumReconnectsReached() throws Exception {
		when(statementMock.execute(any(String.class))).thenThrow(new SQLException());
		try {
			sqlService.createSqlConnection();
			fail("Should not get here");
		} catch (BrevTechnicalException e) {
			assertTrue(e.isFeilkode(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG));
		}
		verify(dataSourceMock, times(SQLService.MAX_CONNECTION_ATTEMPTS)).getConnection(null, null);
		verify(connectionMock, times(SQLService.MAX_CONNECTION_ATTEMPTS)).close();
	}

	private void mockConfigManager() {
		mockStatic(ConfigManager.class);
		when(ConfigManager.getInstance()).thenReturn(configManagerMock);
	}

	private void mockLog() throws Exception {
		mockStatic(Log.class);
		Log logMock = mock(Log.class);
		whenNew(Log.class).withParameterTypes(Class.class).withArguments(any()).thenReturn(logMock);
	}
}
