package no.nav.brevserver.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.jndi.JndiHelper;
import no.nav.brevserver.server.common.log.Log;

/**
 * Klasse med metoder for å åpne og lukke SQL-tilkoblinger.
 *
 * @author Marius Thøring, Visma Consulting
 */
public abstract class SQLService {

	static final int MAX_CONNECTION_ATTEMPTS = 3;

	private Log log = new Log(this.getClass());

	private DataSource datasource;
	private String username;
	private String password;

	private String db2SingleRowOptimization = " for read only optimize for 1 row with UR";

	protected SQLService() {
		username = ConfigManager.getInstance().getString(ConfigManager.DATABASE_USERNAME, null);
		password = ConfigManager.getInstance().getString(ConfigManager.DATABASE_PASSWORD, null);
	}

	protected Connection createSqlConnection() throws BrevTechnicalException {
		if (datasource == null) {
			datasource = JndiHelper.getInstance().lookup(DataSource.class, ConfigManager.DATABASE_JNDI);
		}
		try {
			Connection connection = createValidSqlConnection();
			return connection;
		} catch (SQLException e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		}
	}

	private Connection createValidSqlConnection() throws BrevTechnicalException, SQLException {
		String methSig = "validate()";
		boolean isValid = false;

		Connection con = null;
		for (int i = 1; !isValid && i <= MAX_CONNECTION_ATTEMPTS; i++) {
			con = datasource.getConnection(username, password);
			if (validate(con)) {
				con.setAutoCommit(false);
				isValid = true;
			} else {
				close(methSig, con, Log.DEBUG);
				log.debug(methSig, "Tilkoblingen er ikke gyldig, forsøker å lukke og hente ny, forsøk " + i + "/"
						+ MAX_CONNECTION_ATTEMPTS);
			}
		}
		if (!isValid) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG,
					"Tilkobling til DB2 feilet, ga opp etter " + MAX_CONNECTION_ATTEMPTS + " forsøk");
		}
		return con;
	}

	private boolean validate(Connection con) {
		try {
			Statement stmt = con.createStatement();
			boolean success = stmt.execute("SELECT 1 FROM SYSIBM.SYSDUMMY1");
			stmt.close();
			return success;
		} catch (Exception e) {
			return false;
		}
	}

	protected void close(String methSig, Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				log.error(methSig, "Greide ikke å lukke " + statement.getClass().getSimpleName(), e);
			}
		}
	}

	protected void close(String methSig, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.error(methSig, "Greide ikke å lukke " + rs.getClass().getSimpleName(), e);
			}
		}
	}

	protected void close(String methSig, Connection con, int importance) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				log.write(methSig, "Greide ikke å lukke " + con.getClass().getSimpleName(), importance, e);
			}
		}
	}

	protected void close(String methSig, Connection con) {
		close(methSig, con, Log.ERROR);
	}


	public String getDb2SingleRowOptimization() {
		return db2SingleRowOptimization;
	}

	public void setDb2SingleRowOptimization(String db2SingleRowOptimization) {
		this.db2SingleRowOptimization = db2SingleRowOptimization;
	}
}
