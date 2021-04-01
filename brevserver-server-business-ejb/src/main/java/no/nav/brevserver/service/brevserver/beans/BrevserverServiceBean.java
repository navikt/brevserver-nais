package no.nav.brevserver.service.brevserver.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import no.nav.brevserver.server.common.cache.CacheManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.utility.KnappStatusUtil;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.SysTilgangVO;
import no.nav.brevserver.service.SQLService;
import no.nav.brevserver.service.brevserver.BrevserverService;

/**
 * Implementasjonen av brevserveren. Se metodebeskrivelsene for detaljer.
 * 
 */
public class BrevserverServiceBean extends SQLService implements BrevserverService {
	
	/**
	 * Lagrer token i t_brevtilgang
	 */
	public boolean lagreTilgang(String systemId, String brevreferanse, String token) throws BrevTechnicalException {
		String methSig = "BrevserverServiceBean.lagreTilgang(" + brevreferanse + ")";

		Connection con = null;
		PreparedStatement stmt = null;

		PerformanceLogger p = new PerformanceLogger(methSig);

		try {
			con = createSqlConnection();
			stmt = con.prepareStatement("INSERT INTO T_BREVTILGANG (BREVREFERANSE,SystemID,Token,Timestamp) VALUES (?,?,?,"
					+ Konstanter.TIMESTAMP_SQL + ")");
			stmt.setString(1, brevreferanse);
			stmt.setString(2, systemId);
			stmt.setString(3, token);
			if (stmt.executeUpdate() != 1) {
				con.rollback();
				throw new BrevTechnicalException("Feil antall rader ble forsøkt opprettet i T_BREVTILGANG for: " + systemId
						+ ":" + brevreferanse);
			}
			con.commit();
			return true;

		} catch (java.sql.SQLException e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);

		} finally {
			close(methSig, stmt);
			close(methSig, con);

			p.stop();
		}
	}

	/**
	 * Sjekker tilgang.
	 */
	public boolean sjekkTilgang(String systemId, String brevreferanse, String token) throws BrevTechnicalException {

		String methodSig = "BrevserverServiceBean.sjekkTilgang(" + brevreferanse + ")";

		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		boolean result = false;

		PerformanceLogger p = new PerformanceLogger(methodSig);

		try {
			con = createSqlConnection();
			stmt = con.prepareStatement("SELECT token FROM T_Brevtilgang where BREVREFERANSE = ?" + " and systemid= ?"
					+ getDb2SingleRowOptimization());
			stmt.setString(1, brevreferanse);
			stmt.setString(2, systemId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				if (token != null && token.equals(rs.getString("token"))) {
					result = true;
				}
			}

		} catch (java.sql.SQLException e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);

		} finally {
			close(methodSig, rs);
			close(methodSig, stmt);
			close(methodSig, con);

			p.stop();
		}
		return result;
	}

	/**
	 * Sjekker tilgang for saksbehandlingssystemer
	 */
	public boolean sjekkSystemTilgang(String systemId, String passord) throws BrevTechnicalException {

		String methodSig = "BrevserverServiceBean.sjekkSystemTilgang(" + systemId + ")";
		PerformanceLogger p = new PerformanceLogger(methodSig);

		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String passordCached = (String) CacheManager.getObject(methodSig);
		if (passordCached != null) {
			return passordCached.equals(passord);
		} else {
			try {
				con = createSqlConnection();
				stmt = con.prepareStatement("SELECT systempassord FROM T_BrevSysTilgang where systemid=?"
						+ getDb2SingleRowOptimization());

				stmt.setString(1, systemId);
				rs = stmt.executeQuery();

				if (rs.next()) {
					String syspassord = rs.getString("systempassord");
					CacheManager.addObject(methodSig, syspassord);
					return syspassord.equals(passord);
				} else {
					return false;
				}
			} catch (java.sql.SQLException e) {
				throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
			} finally {
				close(methodSig, rs);
				close(methodSig, stmt);
				close(methodSig, con);
				p.stop();
			}
		}
	}

	private BrevStatusVO hentBrevStatus(String systemId, String brevreferanse, Connection con) throws BrevTechnicalException {
		String methodSig = "BrevserverServiceBean.hentBrevStatus(" + brevreferanse + ")";
		PerformanceLogger p = new PerformanceLogger(methodSig);

		BrevStatusVO brevStatus = null;

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.prepareStatement("SELECT * FROM T_BrevStatus WHERE brevreferanse = ?" + " AND systemid=?"
					+ getDb2SingleRowOptimization());

			stmt.setString(1, brevreferanse);
			stmt.setString(2, systemId);
			rs = stmt.executeQuery();

			boolean fantStatus = rs.next();

			if (fantStatus) {
				brevStatus = new BrevStatusVO();
				brevStatus.setBrevreferanse(brevreferanse);
				brevStatus.setSystemID(systemId);
				brevStatus.setReturKoe(rs.getString("RETURKOE"));
				brevStatus.setBestillerBrukerID(rs.getString("bestillerbrukerid"));
				brevStatus.setBrevmal(rs.getString("brevmal"));
				brevStatus.setStatus(rs.getString("STATUS"));
				brevStatus.setArkiver(rs.getString("ARKIVER"));
				brevStatus.setFormat(rs.getString("FORMAT"));
				brevStatus.setSkriver(rs.getString("SKRIVER"));
				brevStatus.setSkrivertype(rs.getString("SKRIVERTYPE"));
				brevStatus.setSkuff(rs.getString("SKUFF"));

				brevStatus.setKnappStatus(KnappStatusUtil.getKnappStatus(brevStatus.getBrevmal()));
			}
		} catch (java.sql.SQLException e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		} finally {
			close(methodSig, rs);
			close(methodSig, stmt);

			p.stop();
		}

		return brevStatus;
	}
	
	public BrevStatusVO hentBrevStatus(String systemId, String brevreferanse) throws BrevTechnicalException {
		String methSig = "BrevserverServiceBean.hentBrevStatus(" + brevreferanse + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);

		Connection con = null;
		try {
			con = createSqlConnection();
			return hentBrevStatus(systemId, brevreferanse, con);
		} finally {
			close(methSig, con);
			p.stop();
		}
	}
	
	public BrevStatusVO lagreBrevStatus(BrevStatusVO brevStatus) throws BrevTechnicalException {
		String methSig = "BrevserverServiceBean.hentBrevStatus(" + brevStatus.getBrevreferanse() + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);

		Connection con = null;
		try {
			con = createSqlConnection();
			return lagreBrevStatus(brevStatus, con);
		} finally {
			close(methSig, con);
			p.stop();
		}
	}

	/**
	 * Lagrer brevstatus. Hvis brevstatus allerede eksistere blir den oppdatert. Hvis token er inkludert i brevstatusVO vil
	 * denne bli lagret
	 */
	public BrevStatusVO lagreBrevStatus(BrevStatusVO brevStatus, Connection con) throws BrevTechnicalException {

		String methSig = "BrevserverServiceBean.lagreBrevStatus(" + brevStatus.getBrevreferanse() + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);

		PreparedStatement stmt = null;

		BrevStatusVO gmlStatus = null;
		try {
			// Sjekk om vi allerede har status.
			gmlStatus = hentBrevStatus(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), con);
			if (gmlStatus != null) {

				// vi har status, sjekk om det er noen felter som ikke er satt i brevStatus, legg inn gamle verdier hvis ikke
				if (brevStatus.getReturKoe() == null) {
					brevStatus.setReturKoe(gmlStatus.getReturKoe());
				}
				if (brevStatus.getBestillerBrukerID() == null) {
					brevStatus.setBestillerBrukerID(gmlStatus.getBestillerBrukerID());
				}
				if (brevStatus.getBrevmal() == null) {
					brevStatus.setBrevmal(gmlStatus.getBrevmal());
				}
				if (brevStatus.getArkiver() == null) {
					brevStatus.setArkiver(gmlStatus.getArkiver());
				}
				if (brevStatus.getFormat() == null) {
					brevStatus.setFormat(gmlStatus.getFormat());
				}
				if (brevStatus.getSkrivertype() == null) {
					brevStatus.setSkrivertype(gmlStatus.getSkrivertype());
				}
				if (brevStatus.getSkriver() == null) {
					brevStatus.setSkriver(gmlStatus.getSkriver());
				}
				if (brevStatus.getSkuff() == null) {
					brevStatus.setSkuff(gmlStatus.getSkuff());
				}
			}

			if (gmlStatus == null) {
				stmt = con
						.prepareStatement("INSERT INTO T_BREVSTATUS (brevreferanse,systemid,returkoe,bestillerbrukerid,brevmal,status,format,skrivertype,skriver,arkiver,skuff,timestamp) VALUES (?,?,?,?,?,?,?,?,?,?,?,"
								+ Konstanter.TIMESTAMP_SQL + ")");

				String koe = (brevStatus.getReturKoe() == null) ? "" : brevStatus.getReturKoe();
				String mal = (brevStatus.getBrevmal() == null) ? "" : brevStatus.getBrevmal();

				stmt.setString(1, brevStatus.getBrevreferanse());
				stmt.setString(2, brevStatus.getSystemID());
				stmt.setString(3, koe);
				stmt.setString(4, brevStatus.getBestillerBrukerID());
				stmt.setString(5, mal);
				stmt.setString(6, brevStatus.getStatus());
				stmt.setString(7, brevStatus.getFormat());
				stmt.setString(8, brevStatus.getSkrivertype());
				stmt.setString(9, brevStatus.getSkriver());
				stmt.setString(10, getValueOrDefault(brevStatus.getArkiver(), "JA"));
				stmt.setString(11, brevStatus.getSkuff());

				if (stmt.executeUpdate() != 1) {
					con.rollback();
					throw new BrevTechnicalException("Feil antall rader ble forsøkt opprettet i T_BREVSTATUS for: "
							+ brevStatus.getSystemID() + ":" + brevStatus.getBrevreferanse());
				}
				con.commit();
			} else {
				stmt = con
						.prepareStatement("UPDATE T_BREVSTATUS set returkoe=? ,bestillerbrukerid=?, brevmal=?,status=?,format=?,skrivertype=?,skriver=?,arkiver=?,skuff=?,timestamp="
								+ Konstanter.TIMESTAMP_SQL + " " + " WHERE brevreferanse=? and systemid=?");

				String koe = (brevStatus.getReturKoe() == null) ? "" : brevStatus.getReturKoe();
				String mal = (brevStatus.getBrevmal() == null) ? "" : brevStatus.getBrevmal();

				stmt.setString(1, koe);
				stmt.setString(2, brevStatus.getBestillerBrukerID());
				stmt.setString(3, mal);
				stmt.setString(4, brevStatus.getStatus());
				stmt.setString(5, brevStatus.getFormat());
				stmt.setString(6, brevStatus.getSkrivertype());
				stmt.setString(7, brevStatus.getSkriver());
				stmt.setString(8, brevStatus.getArkiver());
				stmt.setString(9, brevStatus.getSkuff());

				stmt.setString(10, brevStatus.getBrevreferanse());
				stmt.setString(11, brevStatus.getSystemID());

				if (stmt.executeUpdate() != 1) {
					con.rollback();
					throw new BrevTechnicalException("Feil antall rader ble forsøkt oppdatert i T_BREVSTATUS for: "
							+ brevStatus.getSystemID() + ":" + brevStatus.getBrevreferanse());
				}
				con.commit();
			}

			if (brevStatus.getToken() != null) {
				lagreTilgang(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken());
			}

		} catch (java.sql.SQLException e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);

		} finally {
			close(methSig, stmt);
			p.stop();
		}

		return gmlStatus;
	}

	private String getValueOrDefault(String value, String def) {
		if (value != null || "".equals(value)) {
			return value;
		} else {
			return def;
		}
	}

	public SysTilgangVO hentTilgang(String systemid, boolean useCache) throws BrevTechnicalException {
		String methSig = "BrevserverServiceBean.hentTilgang(" + systemid + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);

		SysTilgangVO result = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection con = null;

		try {
			// First check the cache
			if (useCache) {
				SysTilgangVO tmp = (SysTilgangVO) CacheManager.getObject(methSig);
				if (tmp != null) {
					return (SysTilgangVO) tmp.clone();
				}
			}

			// Check the database
			con = createSqlConnection();
			stmt = con.prepareStatement("SELECT * FROM T_BrevSysTilgang WHERE SYSTEMID = ?");
			stmt.setString(1, systemid);
			rs = stmt.executeQuery();

			if (rs.next()) {
				result = new SysTilgangVO();
				result.setSysId(rs.getString("systemid"));
				result.setPwd(rs.getString("systempassord"));

				// Add to cache
				CacheManager.addObject(methSig, result);
			}

		} catch (java.sql.SQLException e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);

		} finally {
			close(methSig, rs);
			close(methSig, stmt);
			close(methSig, con);

			p.stop();
		}

		return result;
	}
}
