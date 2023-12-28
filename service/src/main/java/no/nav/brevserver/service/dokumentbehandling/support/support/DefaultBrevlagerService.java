package no.nav.brevserver.service.dokumentbehandling.support.support;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevFinnesAlleredeException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.repository.BrevRepository;
import no.nav.brevserver.core.utils.xmlHandlers.XMLService;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.KvitteringVO;
import no.nav.brevserver.joark.JoarkService;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import no.nav.brevserver.service.converter.BrevTilVoConverter;
import no.nav.brevserver.service.converter.VoTilBrevConverter;
import no.nav.brevserver.service.queue.KvitteringService;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

import static no.nav.brevserver.core.constants.Konstanter.BREVLAGER_STATUS_FERDIG;
import static no.nav.brevserver.core.constants.Konstanter.BREVLAGER_STATUS_KLADD;
import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_AVBRUTT;
import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_FERDIG;
import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_UTSKRIFT;
import static no.nav.brevserver.core.constants.Konstanter.CONTENTTYPE_DOCX_SHORT;
import static no.nav.brevserver.core.constants.Konstanter.SKRIVERTYPE_INGEN;
import static no.nav.brevserver.core.constants.SystemType.BI;
import static no.nav.brevserver.core.constants.SystemType.PE;
import static no.nav.brevserver.core.exception.BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG;
import static no.nav.brevserver.core.vo.FilType.DOCX;
import static no.nav.brevserver.core.vo.FilType.PDF;
import static no.nav.brevserver.core.vo.FilType.RTF;

@Service
@Transactional
@Slf4j
public class DefaultBrevlagerService implements BrevlagerService {

	private static String LAGER_STATUS_A = "A";
	private final byte[] PDF_MED_FORKLARING;
	private final JoarkService joarkService;
	private final BrevstatusService brevstatusService;
	private final BrevRepository brevRepository;
	private final DefaultBrevlagerHistorikkService defaultBrevlagerHistorikkService;
	private final BrevTilVoConverter brevTilVoConverter;
	private final VoTilBrevConverter voTilBrevConverter;
	private final BrevtilgangService brevtilgangService;
	private final KvitteringService kvitteringService;

	public DefaultBrevlagerService(JoarkService joarkService,
								   BrevTilVoConverter brevTilVoConverter,
								   BrevstatusService brevstatusService,
								   BrevRepository brevRepository,
								   VoTilBrevConverter voTilBrevConverter,
								   DefaultBrevlagerHistorikkService defaultBrevlagerHistorikkService,
								   BrevtilgangService brevtilgangService,
								   KvitteringService kvitteringService) throws IOException {
		PDF_MED_FORKLARING = IOUtils.resourceToByteArray("/static/rtf-konvertering-sanert-forklaring.pdf");
		this.joarkService = joarkService;
		this.brevRepository = brevRepository;
		this.brevTilVoConverter = brevTilVoConverter;
		this.defaultBrevlagerHistorikkService = defaultBrevlagerHistorikkService;
		this.voTilBrevConverter = voTilBrevConverter;
		this.brevtilgangService = brevtilgangService;
		this.kvitteringService = kvitteringService;
		this.brevstatusService = brevstatusService;
	}


	@Override
	public BrevVO getBrev(String systemID, String brevReferanse) throws BrevTechnicalException {
		BrevVO brevVO = null;
		try {
			Optional<Brev> brevOpt = brevRepository.findById(BrevreferanseSystemCompositeId.builder().systemId(systemID).brevreferanse(brevReferanse).build());
			if (brevOpt.isPresent()) {
				brevVO = brevTilVoConverter.convert(brevOpt.get());
				translateContentTypeDocxFromDb2(brevVO);
			}
		} catch (RuntimeException e) {
			throw new BrevTechnicalException(DATABASE_IKKE_TILGJENGELIG, e);
		}
		return brevVO;
	}

	@Override
	public BrevStatusVO lagreBrev(BrevVO brev, BrevStatusVO brevstatus) throws BrevTechnicalException {
		try {
			backupIfExistingBrev(brev.getBrevreferanse(), brev.getSystemID());
			BrevStatusVO gmlStatus = brevstatusService.lagreBrevStatus(brevstatus);
			translateContentTypeDocxToDb2(brev);

			if (brevstatus.getSystemID() != null && brevstatus.getSystemID().startsWith(PE.toString())) {
				joarkService.lagreDokument(brevstatus.getBrevreferanse(), brev.getContentType(), brev.getBrevdata());
			} else {
				brevRepository.save(voTilBrevConverter.convert(brev));
			}

			return gmlStatus;
		} catch (RuntimeException e) {
			throw new BrevTechnicalException(DATABASE_IKKE_TILGJENGELIG, e);
		}
	}

	@Override
	public void ferdigstillBrev(BrevStatusVO brevStatus, BrevVO redBrev, BrevVO pdfBrev) throws BrevException {
		try {
			log.info("prøver å ferdigstille brev " + brevStatus.getBrevreferanse() + " fra " + brevStatus.getSystemID() + " mal: " + brevStatus.getBrevmal());

			verifyChangeRequest(brevStatus);
			brevStatus.setStatus(BREVSTATUS_FERDIG);
			brevStatus.setSkrivertype(SKRIVERTYPE_INGEN);
			redBrev.setLagerStatus(BREVLAGER_STATUS_KLADD);
			pdfBrev.setLagerStatus(BREVLAGER_STATUS_FERDIG);

			if (brevStatus.getSystemID().startsWith(PE.toString())) {
				joarkService.lagreFerdigstiltDokument(brevStatus.getBrevreferanse(), redBrev, pdfBrev);
			} else {
				brevferdigstillBrevlagerDokument(brevStatus, redBrev, pdfBrev);
			}

			brevstatusService.lagreBrevStatus(brevStatus);

			log.info("Ferdigstilte brev " + brevStatus.getBrevreferanse() + " fra " + brevStatus.getSystemID() + " mal: " + brevStatus.getBrevmal());
			SystemType systemType = brevStatus.getSystemID().startsWith("PE") ? PE : BI;
			kvitteringService.sendKvittering(pdfBrev, brevStatus, systemType, brevStatus.getReturKoe());
		} catch (RuntimeException e) {
			throw new BrevTechnicalException(DATABASE_IKKE_TILGJENGELIG, e);
		}
	}

	@Override
	public BrevVO hentDokumentFromBrevlagerOrJoark(BrevStatusVO brevStatus) throws BrevTechnicalException {
		checkRequiredFields(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken());
		log.info("hentDokumentFromBrevlagerOrJoark: " + brevStatus.getBrevreferanse() + " fra " + brevStatus.getSystemID() + " mal: " + brevStatus.getBrevmal());

		BrevVO result = null;
		if (sjekkSystemTokenTilgang(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken())) {
			if (brevStatus.getSystemID().startsWith(PE.toString())) {
				result = hentDokumentFraJOARK(brevStatus.getBrevreferanse());
			} else {
				result = getBrev(brevStatus.getSystemID(), brevStatus.getBrevreferanse());
			}
			log.info("hentDokumentFromBrevlagerOrJoark har hentet " + brevStatus.getBrevreferanse() + " fra " + brevStatus.getSystemID());
		} else {
			log.warn("hentDokumentFromBrevlagerOrJoark: Bruker har ikke tilgang til brev med brevreferanse=" + brevStatus.getBrevreferanse() + " fra=" + brevStatus.getSystemID());
		}

		return result;
	}

	private void brevferdigstillBrevlagerDokument(BrevStatusVO brevStatus, BrevVO redBrevVo, BrevVO pdfBrevVo) throws BrevTechnicalException {
		log.info("Prøver å ferdigstille brevlagerdokument " + brevStatus.getBrevreferanse() + " fra " + brevStatus.getSystemID() + " mal: " + brevStatus.getBrevmal());

		translateContentTypeDocxToDb2(redBrevVo);
		Brev redBrev = voTilBrevConverter.convert(redBrevVo);
		Brev pdfBrev = voTilBrevConverter.convert(pdfBrevVo);
		backupIfExistingBrev(pdfBrevVo.getBrevreferanse(), pdfBrevVo.getSystemID());
		defaultBrevlagerHistorikkService.insertHistorikk(redBrev);
		brevRepository.save(pdfBrev);

		log.info("brevlagerdokument " + brevStatus.getBrevreferanse() + " fra " + brevStatus.getSystemID() + " har blitt ferdigstilt");
	}

	@Override
	/*
	 * Brukes bare av bidrag??
	 * Se avbrytDokument i LagreCOntrollerDelegate.java i gamle brevserver
	 */
	public void avbrytDokument(BrevStatusVO brevStatus) throws BrevException {
		if (brevStatus == null) {
			throw new IllegalArgumentException("Brevstatus er null!");
		}

		verifyChangeRequest(brevStatus);
		brevStatus.setStatus(BREVSTATUS_AVBRUTT);
		brevstatusService.lagreBrevStatus(brevStatus);

		if (brevStatus.getReturKoe() != null) {
			KvitteringVO kvittering = new KvitteringVO();
			kvittering.setSystemID(brevStatus.getSystemID());
			kvittering.setBrevreferanse(brevStatus.getBrevreferanse());
			brevStatus.setStatus(BREVSTATUS_AVBRUTT);

			String xmlKvittering = XMLService.unmarshal(kvittering, brevStatus);
			log.info("Sender kvittering for brevreferanse=" + brevStatus.getBrevreferanse());
			kvitteringService.sendKvitteringBi(xmlKvittering, brevStatus.getReturKoe());
		}

		log.info("Brevet ble avbrutt. Brevref: " + brevStatus.getBrevreferanse());
	}

	@Override
	public void lagreDokument(BrevVO brev, BrevStatusVO brevStatusVO, SystemType systemType) throws BrevException {
		log.info("Lagrer dokument " + brevStatusVO.getBrevreferanse() + " fra " + brevStatusVO.getSystemID() + " mal: " + brevStatusVO.getBrevmal());
		if (brev == null || brevStatusVO == null) {
			throw new IllegalArgumentException("Brevstatus er null!");
		}

		verifyChangeRequest(brevStatusVO);
		if (brevStatusVO.getSystemID().startsWith(PE.toString())) {
			lagreJoarkDokument(brev);
		} else {
			lagreBrev(brev, brevStatusVO);
		}

		kvitteringService.sendKvittering(brev, brevStatusVO, systemType, brevStatusVO.getReturKoe());
	}


	private void backupIfExistingBrev(String brevreferanse, String systemID) throws BrevTechnicalException {
		Optional<Brev> brevOpt = brevRepository.findById(BrevreferanseSystemCompositeId.builder().systemId(systemID).brevreferanse(brevreferanse).build());

		if (brevOpt.isPresent()) {
			Brev brev = brevOpt.get();
			if (brev.getStatus().equals(BREVLAGER_STATUS_FERDIG)) {
				throw new BrevTechnicalException("Brevet har status = '" + BREVLAGER_STATUS_FERDIG + "' og kan ikke endres");
			}
			defaultBrevlagerHistorikkService.insertHistorikk(brev);
		}
	}

	private void lagreJoarkDokument(BrevVO brev) {
		try {
			joarkService.lagreDokument(brev.getBrevreferanse(), brev.getContentType(), brev.getBrevdata());
		} catch (BrevException e) {
			e.printStackTrace();
		}
	}

	protected void verifyChangeRequest(BrevStatusVO brevStatusVO) throws BrevException {
		checkRequiredFields(brevStatusVO.getSystemID(), brevStatusVO.getBrevreferanse(), brevStatusVO.getToken());
		BrevStatusVO oldBrevStatus = brevstatusService.hentBrevStatus(brevStatusVO.getBrevreferanse(), brevStatusVO.getSystemID());
		if (oldBrevStatus != null) {
			sjekkSystemTokenTilgang(brevStatusVO.getSystemID(), brevStatusVO.getBrevreferanse(), brevStatusVO.getToken());
			verifyEditableStatus(oldBrevStatus);
		}
	}

	private void verifyEditableStatus(BrevStatusVO brevStatus) throws BrevException {
		if (BREVSTATUS_FERDIG.equals(brevStatus.getStatus()) || BREVSTATUS_UTSKRIFT.equals(brevStatus.getStatus())) {
			throw new BrevFinnesAlleredeException("Brevet med brevreferanse " + brevStatus.getBrevreferanse() + " har status " + brevStatus.getStatus() + " og kan ikke endres");
		}
	}

	private boolean sjekkSystemTokenTilgang(String systemID, String brevreferanse, String token) throws BrevTechnicalException {
		return brevtilgangService.sjekkTilgang(systemID, brevreferanse, token);
	}

	private BrevVO hentDokumentFraJOARK(String journalpostId) throws BrevTechnicalException {
		BrevVO result = joarkService.hentDokument(journalpostId);

		if (LAGER_STATUS_A.equals(result.getLagerStatus()) && result.getContentType().equals(RTF.getContentType())) {
			log.warn("Forsøkt hentet avbrutt brev med journalpostId={} med contentType=RTF. " +
					 "Returnerer i stedet pdf med forklaring på hvorfor RTF til PDF konvertering ikke fungerer lenger", result.getBrevreferanse());
			return statiskPdfMedForklaring(journalpostId);
		}
		return result;
	}

	private BrevVO statiskPdfMedForklaring(String brevreferanse) throws BrevTechnicalException {
		BrevVO brevVO = new BrevVO();
		brevVO.setBrevreferanse(brevreferanse);
		brevVO.setLagerStatus(LAGER_STATUS_A);
		brevVO.setContentType(PDF.getContentType());
		brevVO.setBrevdata(PDF_MED_FORKLARING);
		return brevVO;
	}

	private void translateContentTypeDocxToDb2(BrevVO brevVO) {
		if (brevVO.getContentType() != null && brevVO.getContentType().equals(DOCX.getContentType())) {
			brevVO.setContentType(CONTENTTYPE_DOCX_SHORT);
		}
	}

	private void translateContentTypeDocxFromDb2(BrevVO brevVO) {
		if (brevVO.getContentType() != null && brevVO.getContentType().equals(CONTENTTYPE_DOCX_SHORT)) {
			brevVO.setContentType(DOCX.getContentType());
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

}
