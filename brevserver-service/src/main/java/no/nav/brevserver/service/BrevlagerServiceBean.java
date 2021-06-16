package no.nav.brevserver.service;

import no.nav.brevserver.consumer.joark.JoarkServiceBi;
import no.nav.brevserver.consumer.joark.factory.JoarkServiceBeanFactory;
import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.repository.BrevRepository;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.to.AvbrytDokumentRequest;
import no.nav.brevserver.server.common.to.LagreDokumentRequest;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.ArgumentValidator;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.service.converter.BrevTilVoConverter;
import no.nav.brevserver.service.converter.BrevstatusTilVoConverter;
import no.nav.brevserver.service.converter.FileConverter;
import no.nav.brevserver.service.converter.VoTilBrevConverter;
import no.nav.brevserver.service.converter.VoTilBrevstatusConverter;
import no.nav.brevserver.service.queue.KoService;
import no.nav.brevserver.service.queue.xml.XMLService;
import no.nav.brevserver.service.queue.xml.XMLServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrevlagerServiceBean implements BrevlagerService {

	private static String LAGER_STATUS_A = "A";
	private final BrevserverServiceBean brevserverServiceBean;
	private final BrevstatusServiceBean brevstatusServiceBean;
	private final BrevRepository brevRepository;
	private final BrevlagerHistorikkServiceBean brevlagerHistorikkServiceBean;
	private final BrevTilVoConverter brevTilVoConverter;
	private final VoTilBrevConverter voTilBrevConverter;
	private final VoTilBrevstatusConverter voTilBrevstatusConverter;
	private final BrevstatusTilVoConverter brevstatusTilVoConverter;
	private final KoService koService;

	@Autowired
	public BrevlagerServiceBean(BrevTilVoConverter brevTilVoConverter,
								BrevserverServiceBean brevserverServiceBean,
								BrevstatusServiceBean brevstatusServiceBean,
								BrevRepository brevRepository,
								VoTilBrevConverter voTilBrevConverter,
								BrevlagerHistorikkServiceBean brevlagerHistorikkServiceBean,
								VoTilBrevstatusConverter voTilBrevstatusConverter,
								BrevstatusTilVoConverter brevstatusTilVoConverter,
								KoService koService) {
		this.brevRepository = brevRepository;
		this.brevserverServiceBean = brevserverServiceBean;
		this.brevTilVoConverter = brevTilVoConverter;
		this.voTilBrevstatusConverter = voTilBrevstatusConverter;
		this.brevlagerHistorikkServiceBean = brevlagerHistorikkServiceBean;
		this.voTilBrevConverter = voTilBrevConverter;
		this.brevstatusTilVoConverter = brevstatusTilVoConverter;
		this.koService = koService;
		this.brevstatusServiceBean = brevstatusServiceBean;
	}


	@Override
	public BrevVO getBrev(String systemID, String brevReferanse) throws BrevTechnicalException {
		BrevVO brevVO = null;
		try {
			List<Brev> brevListe = brevRepository.findBySystemIdAndBrevreferanse(systemID, brevReferanse);
			if (brevListe.size() > 0) {
				brevVO = brevTilVoConverter.convert(brevListe.get(0));
				// translateContentTypeDocxFromDb2(brevVO);
			}
		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		}
		return brevVO;
	}

	@Override
	public BrevVO hentDokumentFromBrevlagerOrJoark(BrevStatusVO brevStatus) throws BrevTechnicalException, BrevFunctionalException {
		checkRequiredFields(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken());
		BrevVO result = null;
		if (sjekkSystemTokenTilgang(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken())) {
			if (brevStatus.getSystemID().startsWith(SystemType.PE.toString())) {
				result = hentDokumentFraJOARK(brevStatus.getBrevreferanse());
			} else {
				result = getBrev(brevStatus.getSystemID(), brevStatus.getBrevreferanse());
			}
		}
		return result;
	}

	@Override
	public void ping() {

	}

	@Override
	public void lagreDokument(LagreDokumentRequest request) throws BrevException {
		BrevVO brev = request.getBrev();
		BrevStatusVO brevStatus = request.getBrevStatus();

		SystemType systemType = brevStatus.getSystemID().startsWith("PE") ? SystemType.PE : SystemType.BI;

		brev.setLagerStatus(Konstanter.BREVLAGER_STATUS_KLADD);
		brevStatus.setStatus(Konstanter.BREVSTATUS_LAGRET_KLADD);

		lagreDokument(brev, brevStatus, systemType);
	}

	@Override
	public void avbrytDokument(AvbrytDokumentRequest avbrytDokumentRequest) throws BrevException {
		avbrytDokumentRequest.validate();
		BrevStatusVO brevStatus = avbrytDokumentRequest.getBrevStatus();
		verifyChangeRequest(brevStatus);
		brevStatus.setStatus(Konstanter.BREVSTATUS_AVBRUTT);
		//TODO: FIX
		//brevstatusServiceBean.lagreDokumentStatus(brevStatus);

		if (brevStatus.getReturKoe() != null) {
			KvitteringVO kvittering = new KvitteringVO();
			kvittering.setSystemID(brevStatus.getSystemID());
			kvittering.setBrevreferanse(brevStatus.getBrevreferanse());
			brevStatus.setStatus(Konstanter.BREVSTATUS_AVBRUTT);

			XMLService service = XMLServiceFactory.getInstance().createXMLService();
			String xmlKvittering = service.unmarshal(kvittering, brevStatus);
//TODO:FIXME
			//sendKvittering(brevStatus.getReturKoe(), false, null, xmlKvittering);
		}

	}

	private void lagreDokument(BrevVO brev, BrevStatusVO brevStatusVO, SystemType systemType) throws BrevException {
		ArgumentValidator.isNotNull(brev);
		ArgumentValidator.isNotNull(brevStatusVO);
		verifyChangeRequest(brevStatusVO);
		if (brevStatusVO.getSystemID().startsWith(SystemType.PE.toString())) {
			lagreJoarkDokument(brev, brevStatusVO, systemType, brevStatusVO.getReturKoe());
		} else {
			Brevstatus brevstatus = voTilBrevstatusConverter.convert(brevStatusVO);
			lagreBrev(brev, brevstatus, brevStatusVO.getToken());
		}
		koService.sendKvittering(brev, brevStatusVO, systemType, brevStatusVO.getReturKoe());
	}


	public BrevStatusVO lagreBrev(BrevVO brev, Brevstatus brevstatus, String token) throws BrevTechnicalException {
		backupIfExistingBrev(brev.getBrevreferanse(), brev.getSystemID());
		Brevstatus gmlStatus = brevserverServiceBean.lagreBrevStatus(brevstatus, token);
		translateContentTypeDocxToDb2(brev);
		brevRepository.save(voTilBrevConverter.convert(brev));
		return brevstatusTilVoConverter.convert(gmlStatus);
	}

	private boolean backupIfExistingBrev(String brevreferanse, String systemID) throws BrevTechnicalException {

		List<Brev> brevList = brevRepository.findBySystemIdAndBrevreferanse(systemID, brevreferanse);
		if (brevList.size() > 0) {
			if (brevList.get(0).getStatus().equals(Konstanter.BREVLAGER_STATUS_FERDIG)) {
				throw new BrevTechnicalException("Brevet har status = '" + Konstanter.BREVLAGER_STATUS_FERDIG
						+ "' og kan ikke endres");
			}
			brevlagerHistorikkServiceBean.insertHistorikk(brevList.get(0));
			return true;
		} else {
			return false;
		}
	}

	private void lagreJoarkDokument(BrevVO brev, BrevStatusVO brevstatus, SystemType systemType, String returKoe)
			throws BrevTechnicalException {
		JoarkServiceBi joarkService = JoarkServiceBeanFactory.getInstance().getJoarkService();
		joarkService.lagreDokument(brevstatus.getBrevreferanse(), brev.getContentType(), brev.getBrevdata());
	}

	protected void verifyChangeRequest(BrevStatusVO brevStatus) throws BrevException {
		checkRequiredFields(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken());
		Brevstatus oldBrevStatus = brevstatusServiceBean.hentBrevStatus(brevStatus.getSystemID(), brevStatus.getBrevreferanse());
		if (oldBrevStatus != null) {
			sjekkSystemTokenTilgang(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken());
			verifyEditableStatus(oldBrevStatus);
		}
	}

	private void verifyEditableStatus(Brevstatus brevStatus) throws BrevException {
		if (Konstanter.BREVSTATUS_FERDIG.equals(brevStatus.getStatus())
				|| Konstanter.BREVSTATUS_UTSKRIFT.equals(brevStatus.getStatus())) {
			throw new BrevFunctionalException("Brevet med brevreferanse " + brevStatus.getBrevreferanse() + " har status " + brevStatus.getStatus() + " og kan ikke endres");
		}
	}

	private boolean sjekkSystemTokenTilgang(String systemID, String brevreferanse, String token) throws BrevTechnicalException {
		return brevserverServiceBean.sjekkTilgang(systemID, brevreferanse, token);
	}

	private BrevVO hentDokumentFraJOARK(String brevreferanse) throws
			BrevTechnicalException, BrevFunctionalException {
		JoarkServiceBi joarkService = JoarkServiceBeanFactory.getInstance().getJoarkService();
		BrevVO result = joarkService.hentDokument(brevreferanse);

		if (LAGER_STATUS_A.equals(result.getLagerStatus()) && result.getContentType().equals(FilType.RTF.getContentType())) {
			konverterRtfTilPdf(result, brevreferanse);
		}
		return result;
	}

	private void konverterRtfTilPdf(BrevVO result, String brevreferanse) throws BrevTechnicalException {
		try {
			result.setBrevdata(FileConverter.getInstance().convertToPdf(result.getBrevdata()));
			result.setContentType(FilType.PDF.getContentType());
		} catch (Exception e) {
			throw new BrevTechnicalException("Greide ikke å konvertere dokument med brevreferanse " + brevreferanse
					+ " til pdf", e);
		}
	}

	private void translateContentTypeDocxToDb2(BrevVO brevVO) {
		if (brevVO.getContentType() != null && brevVO.getContentType().equals(FilType.DOCX.getContentType())) {
			brevVO.setContentType(Konstanter.CONTENTTYPE_DOCX_SHORT);
		}
	}

	protected void checkRequiredFields(String systemId, String brevreferanse, String token) throws BrevTechnicalException {
		if (systemId == null) {
			throw new BrevTechnicalException("Manglende obligatorisk felt: systemId");
		} else if (brevreferanse == null) {
			throw new BrevTechnicalException("Manglende obligatorisk felt: brevreferanse");
		} else if (token == null) {
			throw new BrevTechnicalException("Manglende obligatorisk felt: token");
		}
	}


/*
	public BrevStatusVO lagreBrev(BrevVO brev, Brevstatus brevstatus, String token) throws BrevTechnicalException {
		String methSig = "BrevlagerServiceBean.lagreDokument(" + brev.getBrevreferanse() + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);
		try {
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

	private void insertBrev(Brev brev) {

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
	}*/
}
