package no.nav.brevserver.service;

import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import no.nav.brevserver.server.common.config.ConfigManager;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.IOUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.fail;

/**
 * Abstract database testclass. Bootstraps an in-memory H2 database, attaching it to the JNDI context.
 * Performs DDL and cleans up for each test. Also provides convenience methods for database query and updates.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public abstract class AbstractDatabaseTest {

	protected static final String CONNECTION_URL = "jdbc:h2:mem:test_brev;MODE=DB2;DB_CLOSE_DELAY=-1";
	protected static final String USERNAME = "sa";
	protected static final String PASSWORD = "";

	private SQLQuery sqlQuery;
	private Configuration sqlConfiguration;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.memory.MemoryContextFactory");
		System.setProperty("org.osjava.sj.jndi.shared", "true");
		System.setProperty(ConfigManager.DATABASE_USERNAME, USERNAME);
		System.setProperty(ConfigManager.DATABASE_PASSWORD, PASSWORD);
		System.setProperty(ConfigManager.DATABASE_URL, CONNECTION_URL);
	}

	public DataSource jndiDataSource() {
		String username = ConfigManager.getInstance().getString(ConfigManager.DATABASE_USERNAME, null);
		String password = ConfigManager.getInstance().getString(ConfigManager.DATABASE_PASSWORD, null);
		String url = ConfigManager.getInstance().getString(ConfigManager.DATABASE_URL, null);
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.ibm.db2.jcc.DB2Driver");
		dataSource.setUrl(url);
		//dataSource.setSchema("BS475Q");
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		return dataSource;
	}

	@Before
	public void setUpDatabase() throws Exception {
		bindJndiContext();
		initialiseDatabase();
	}

	private static void bindJndiContext() {
		try {
			Context context = new InitialContext();
			context.createSubcontext("jdbc");

			JdbcDataSource dataSource = createH2DataSource();

			context.bind(ConfigManager.DATABASE_JNDI, dataSource);
		} catch (NamingException e) {
			// Do nothing
		}
	}

	private static JdbcDataSource createH2DataSource() {
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL(CONNECTION_URL);
		dataSource.setUser(USERNAME);
		dataSource.setPassword(PASSWORD);
		return dataSource;
	}

	private void initialiseDatabase() throws Exception {
		sqlQuery = configQueryDsl();
		createDatabase();
	}

	private SQLQuery configQueryDsl() throws SQLException {
		SQLTemplates templates = new H2Templates();
		sqlConfiguration = new Configuration(templates);
		return new SQLQuery(getConnection(), sqlConfiguration);
	}

	private void createDatabase() throws IOException {
		String sqlQueries = IOUtils.toString(ClassLoader.getSystemResource("create-database.sql"));
		executeSql(sqlQueries);
	}

	private void cleanDatabase() {
		executeSql("drop all objects");
	}

	@After
	public void tearDownDatabase() throws Exception {
		cleanDatabase();
	}

	protected Connection getConnection() {
		try {
			Class.forName ("org.h2.Driver");
			return DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			fail("Unable to get connection");
			throw new RuntimeException(e);
		}
	}

	protected void executeSql(String sql) {
		try {
			new QueryRunner().update(getConnection(), sql);
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Failed to execute SQL");
		}
	}

	protected SQLUpdateClause update(RelationalPath<?> e) {
		return new SQLUpdateClause(getConnection(), sqlConfiguration, e);
	}

	protected SQLInsertClause insert(RelationalPath<?> e) {
		return new SQLInsertClause(getConnection(), sqlConfiguration, e);
	}

	protected SQLDeleteClause delete(RelationalPath<?> e) {
		return new SQLDeleteClause(getConnection(), sqlConfiguration, e);
	}

	protected SQLQuery query() {
		return sqlQuery;
	}
}
