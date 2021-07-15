package no.nav.brevserver.service.brevlager.beans;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.rowset.serial.SerialBlob;

import no.nav.brevserver.converter.BrevstatusTilVoConverter;
import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.repository.BrevRepository;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevRuntimeException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.service.SQLService;
import no.nav.brevserver.service.brevlager.BrevlagerService;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Metoder foråhente, lagre og ta backup av brev i Brevlageret (IBM DB2).
 *
 * @author Marius Thåring, Visma Consulting
 */
public class BrevlagerServiceBean extends SQLService implements BrevlagerService {

	@Autowired
	private BrevRepository brevRepository;
	@Autowired
	private BrevstatusTilVoConverter converter = new BrevstatusTilVoConverter();

	@Override
	public BrevVO getBrev(String systemID, String brevReferanse) throws BrevTechnicalException {
		String methSig = "BrevlagerServiceBean.getBrev(" + brevReferanse + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);

		Connection con = null;
		PreparedStatement stmt = null;
		BrevVO brevVO = null;
		try {
			con = createSqlConnection();
			stmt = getSelectStatementFromBrevlager(con, brevReferanse, systemID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				brevVO = createBrevVOFromCurrentRow(rs);
				translateContentTypeDocxFromDb2(brevVO);
			}
		} catch (SQLException e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		} finally {
			close(methSig, stmt);
			close(methSig, con);
			p.stop();
		}
		return brevVO;
	}

	public BrevStatusVO lagreBrev(BrevVO brev, Brevstatus brevstatus, String token) throws BrevTechnicalException {
		String methSig = "BrevlagerServiceBean.lagreDokument(" + brev.getBrevreferanse() + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);

		Connection con = null;
		try {
			con = createSqlConnection();
			boolean isExistingBrev = backupIfExistingBrev(con, brev.getBrevreferanse(), brev.getSystemID());
			BrevserverService service = BrevserverServiceFactory.getInstance().createBrevserverService();
			Brevstatus gmlStatus = service.lagreBrevStatus(brevstatus, token);
			
			translateContentTypeDocxToDb2(brev);
			if (isExistingBrev) {
				updateBrev(con, brev);
			} else {
				insertBrev(con, brev);
			}

			return converter.convert(gmlStatus);
		} catch (SQLException e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		} finally {
			close(methSig, con);
			p.stop();
		}
	}

	public void ferdigstillBrev(Brevstatus brevstatus, BrevVO redBrevVO, BrevVO pdfBrevVO, String token) throws BrevTechnicalException {
		String methSig = "BrevlagerServiceBean.lagreDokument(" + brevstatus.getBrevreferanse() + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);

		Connection con = null;
		try {
			boolean isExistingBrev = backupIfExistingBrev(con, pdfBrevVO.getBrevreferanse(), pdfBrevVO.getSystemID());
			BrevserverService service = BrevserverServiceFactory.getInstance().createBrevserverService();
			service.lagreBrevStatus(brevstatus, token);

			translateContentTypeDocxToDb2(redBrevVO);
			insertHistorikk(con, redBrevVO);
			if (isExistingBrev) {
				updateBrev(con, pdfBrevVO);
			} else {
				insertBrev(con, pdfBrevVO);
			}
		} catch (SQLException e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		} finally {
			close(methSig, con);
			p.stop();
		}
	}

	@Override
	public void ping() {
		String methSig = "BrevlagerServiceBean.ping()";
		Connection con = null;
		Statement statement = null;
		try {
			con = createSqlConnection();
			statement = con.createStatement();
			statement.execute("select 1 from sysibm.sysdummy1");
		} catch (SQLException e) {
			throw new BrevRuntimeException("Database call failed",
					new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e));
		} catch (BrevTechnicalException e) {
			throw new BrevRuntimeException("Could not create connection", e);
		} finally {
			close(methSig, con);
			close(methSig, statement);
		}
	}

	private void updateBrev(Connection con, BrevVO brev) throws SQLException, BrevTechnicalException {
		String methSig = "BrevlagerServiceBean.updateBrev(" + brev.getBrevreferanse() + ")";

		PreparedStatement stmt = con.prepareStatement("UPDATE " + Konstanter.BREVLAGER_TABELL
				+ " SET status=?, contenttype=?, timestamp=" + Konstanter.TIMESTAMP_SQL
				+ ", brukerid=?, brevdata=? where brevreferanse=? and systemid=?");
		try {
			stmt.setString(1, brev.getLagerStatus());
			stmt.setString(2, brev.getContentType());
			stmt.setString(3, brev.getBrukerID());
			stmt.setBlob(4, new SerialBlob(brev.getBrevdata()));
			stmt.setString(5, brev.getBrevreferanse());
			stmt.setString(6, brev.getSystemID());

			if (stmt.executeUpdate() != 1) {
				con.rollback();
				throw new BrevTechnicalException("Feil antall rader ble forsøkt oppdatert i " + Konstanter.BREVLAGER_TABELL
						+ " for: " + brev.getSystemID() + ":" + brev.getBrevreferanse());
			}
			con.commit();
		} finally {
			close(methSig, stmt);
		}
	}

	private void insertBrev(Brev brev){

	}

	private void insertBrev(Connection con, BrevVO brev) throws SQLException, BrevTechnicalException {
		String methSig = "BrevlagerServiceBean.insertBrev(" + brev.getBrevreferanse() + ")";

		PreparedStatement stmt = con.prepareStatement("INSERT INTO " + Konstanter.BREVLAGER_TABELL
				+ " (brevreferanse, systemid, status, contenttype, timestamp, brukerid, brevdata) " + "VALUES (?, ?, ?, ?, "
				+ Konstanter.TIMESTAMP_SQL + ", ?, ?)");
		try {
			stmt.setString(1, brev.getBrevreferanse());
			stmt.setString(2, brev.getSystemID());
			stmt.setString(3, brev.getLagerStatus());
			stmt.setString(4, brev.getContentType());
			stmt.setString(5, brev.getBrukerID());
			stmt.setBlob(6, new SerialBlob(brev.getBrevdata()));

			if (stmt.executeUpdate() != 1) {
				con.rollback();
				throw new BrevTechnicalException("Feil antall rader ble forsøkt opprettet i " + Konstanter.BREVLAGER_TABELL
						+ " for: " + brev.getSystemID() + ":" + brev.getBrevreferanse());
			}
			con.commit();
		} finally {
			close(methSig, stmt);
		}
	}

	private PreparedStatement getSelectStatementFromBrevlager(Connection con, String brevreferanse, String systemId)
			throws SQLException {
		PreparedStatement stmt = con.prepareStatement("SELECT * from " + Konstanter.BREVLAGER_TABELL
				+ " WHERE BREVREFERANSE=? and systemid=?" + getDb2SingleRowOptimization());
		stmt.setString(1, brevreferanse);
		stmt.setString(2, systemId);
		return stmt;
	}

	private BrevVO createBrevVOFromCurrentRow(ResultSet rs) throws SQLException {
		BrevVO brevVO = new BrevVO();
		brevVO.setBrevreferanse(rs.getString("BREVREFERANSE"));
		brevVO.setSystemID(rs.getString("SYSTEMID"));
		brevVO.setLagerStatus(rs.getString("STATUS"));
		brevVO.setContentType(rs.getString("CONTENTTYPE"));
		brevVO.setBrukerID(rs.getString("BRUKERID"));
		brevVO.setEndret(rs.getTimestamp("TIMESTAMP"));
		Blob blob = rs.getBlob("BREVDATA");
		brevVO.setBrevdata(blob.getBytes(1, (int) blob.length()));
		return brevVO;
	}

	private boolean backupIfExistingBrev(Connection con, String brevreferanse, String systemId) throws SQLException,
			BrevTechnicalException {
		String methSig = "BrevlagerServiceBean.backupIfExistingBrev(" + brevreferanse + ")";

		PreparedStatement stmt = null;
		try {
			stmt = getSelectStatementFromBrevlager(con, brevreferanse, systemId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				if (rs.getString("STATUS").equals(Konstanter.BREVLAGER_STATUS_FERDIG)) {
					throw new BrevTechnicalException("Brevet har status = '" + Konstanter.BREVLAGER_STATUS_FERDIG
							+ "' og kan ikke endres");
				}
				insertHistorikk(con, createBrevVOFromCurrentRow(rs));
				return true;
			} else {
				return false;
			}
		} finally {
			close(methSig, stmt);
		}
	}

	private void insertHistorikk(Connection con, BrevVO brev) throws BrevTechnicalException, SQLException {
		String methSig = "BrevlagerServiceBean.backupBrev(" + brev.getBrevreferanse() + ")";

		PreparedStatement stmt = con
				.prepareStatement("INSERT INTO "
						+ Konstanter.BREVLAGER_HISTORIKK_TABELL
						+ " "
						+ "(BREVLAGER_HISTORIK_ID, BREVREFERANSE, SYSTEMID, BRUKERID, STATUS, CONTENTTYPE, TIMESTAMP, BREVDATA, VASKET) "
						+ "VALUES ((NEXT VALUE FOR " + Konstanter.BREVLAGER_HISTORIKK_TABELL + "_SEQ)" + ",?,?,?,?,?,?,?,'0')");
		try {
			stmt.setString(1, brev.getBrevreferanse());
			stmt.setString(2, brev.getSystemID());
			stmt.setString(3, brev.getBrukerID());
			stmt.setString(4, brev.getLagerStatus());
			stmt.setString(5, brev.getContentType());
			if (brev.getEndret() == null) {
				brev.setEndret(new Timestamp(System.currentTimeMillis()));
			}
			stmt.setTimestamp(6, brev.getEndret());
			stmt.setBlob(7, new SerialBlob(brev.getBrevdata()));

			if (stmt.executeUpdate() != 1) {
				con.rollback();
				throw new BrevTechnicalException("Feil antall rader ble forsøkt opprettet i "
						+ Konstanter.BREVLAGER_HISTORIKK_TABELL + " for: " + brev.getSystemID() + ":" + brev.getBrevreferanse());
			}
			con.commit();
		} finally {
			close(methSig, stmt);
		}
	}

	private void translateContentTypeDocxToDb2(BrevVO brevVO) {
		if (brevVO.getContentType() != null && brevVO.getContentType().equals(FilType.DOCX.getContentType())) {
			brevVO.setContentType(Konstanter.CONTENTTYPE_DOCX_SHORT);
		}
	}

	private void translateContentTypeDocxFromDb2(BrevVO brevVO) {
		if (brevVO.getContentType() != null && brevVO.getContentType().equals(Konstanter.CONTENTTYPE_DOCX_SHORT)) {
			brevVO.setContentType(FilType.DOCX.getContentType());
		}
	}
}