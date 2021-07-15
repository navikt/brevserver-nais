package no.nav.brevserver.service;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.log.Log;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Klasse med metoder forååpne og lukke SQL-tilkoblinger.
 *
 * @author Marius Thåring, Visma Consulting
 */
public abstract class SQLService {

	static final int MAX_CONNECTION_ATTEMPTS = 3;

	private Log log = new Log(this.getClass());

	private DataSource datasource;

	private String db2SingleRowOptimization = " for read only optimize for 1 row with UR";

	protected SQLService() {
	}

	protected Connection createSqlConnection() throws BrevTechnicalException {
		if (datasource == null) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, "Datasource ikke opprettet");
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
			con = datasource.getConnection();
			con.setAutoCommit(false);
			isValid = true;
		}
		return con;
	}

	protected void close(String methSig, Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				log.error(methSig, "Greide ikkeålukke " + statement.getClass().getSimpleName(), e);
			}
		}
	}

	protected void close(String methSig, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.error(methSig, "Greide ikkeålukke " + rs.getClass().getSimpleName(), e);
			}
		}
	}

	protected void close(String methSig, Connection con, int importance) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				log.write(methSig, "Greide ikkeålukke " + con.getClass().getSimpleName(), importance, e);
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
