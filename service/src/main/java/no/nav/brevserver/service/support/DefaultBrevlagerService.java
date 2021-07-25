package no.nav.brevserver.service.support;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.repository.BrevRepository;
import no.nav.brevserver.fagarkiv.dokarkiv.DokarkivConsumer;
import no.nav.brevserver.fagarkiv.saf.SafConsumer;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.ArgumentValidator;
import no.nav.brevserver.service.queue.jms.BIMessageProducer;
import no.nav.brevserver.service.utility.KnappStatusUtil;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import no.nav.brevserver.service.converter.BrevTilVoConverter;
import no.nav.brevserver.service.converter.BrevstatusTilVoConverter;
import no.nav.brevserver.service.converter.FileConverter;
import no.nav.brevserver.service.converter.VoTilBrevConverter;
import no.nav.brevserver.service.converter.VoTilBrevstatusConverter;
import no.nav.brevserver.service.dokumentbehandling.to.AvbrytDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.LagreDokumentRequest;
import no.nav.brevserver.service.queue.KoService;
import no.nav.brevserver.service.queue.xml.XMLService;
import no.nav.brevserver.service.queue.xml.XMLServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class DefaultBrevlagerService implements BrevlagerService {

	private static String LAGER_STATUS_A = "A";

	private final BrevstatusService brevstatusService;
	private final BrevRepository brevRepository;
	private final DefaultBrevlagerHistorikkService defaultBrevlagerHistorikkService;
	private final BrevTilVoConverter brevTilVoConverter;
	private final VoTilBrevConverter voTilBrevConverter;
	private final VoTilBrevstatusConverter voTilBrevstatusConverter;
	private final BrevstatusTilVoConverter brevstatusTilVoConverter;
	private final BrevtilgangService brevtilgangService;
	private final KoService koService;
	private final DokarkivConsumer dokarkivConsumer;
	private final SafConsumer safConsumer;
	private final KnappStatusUtil knappStatusUtil;
	private final BIMessageProducer biMessageProducer;

	@Autowired
	public DefaultBrevlagerService(BrevTilVoConverter brevTilVoConverter,
								   BrevstatusService brevstatusService,
								   BrevRepository brevRepository,
								   VoTilBrevConverter voTilBrevConverter,
								   DefaultBrevlagerHistorikkService defaultBrevlagerHistorikkService,
								   VoTilBrevstatusConverter voTilBrevstatusConverter,
								   BrevstatusTilVoConverter brevstatusTilVoConverter,
								   BrevtilgangService brevtilgangService, KoService koService,
								   DokarkivConsumer dokarkivConsumer,
								   SafConsumer safConsumer,
								   KnappStatusUtil knappStatusUtil,
								   BIMessageProducer biMessageProducer) {
		this.brevRepository = brevRepository;
		this.brevTilVoConverter = brevTilVoConverter;
		this.voTilBrevstatusConverter = voTilBrevstatusConverter;
		this.defaultBrevlagerHistorikkService = defaultBrevlagerHistorikkService;
		this.voTilBrevConverter = voTilBrevConverter;
		this.brevstatusTilVoConverter = brevstatusTilVoConverter;
		this.brevtilgangService = brevtilgangService;
		this.koService = koService;
		this.brevstatusService = brevstatusService;
		this.dokarkivConsumer = dokarkivConsumer;
		this.safConsumer = safConsumer;
		this.knappStatusUtil = knappStatusUtil;
		this.biMessageProducer = biMessageProducer;
	}


	@Override
	public BrevVO getBrev(String systemID, String brevReferanse) throws BrevTechnicalException {
		BrevVO brevVO = null;
		try {
			List<Brev> brevListe = brevRepository.findBySystemIdAndBrevreferanse(systemID, brevReferanse);
			if (brevListe.size() > 0) {
				brevVO = brevTilVoConverter.convert(brevListe.get(0));
				//translateContentTypeDocxFromDb2(brevVO);
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
		//Return databasecheck
	}

	@Override
	public void ferdigstillDokument(FerdigstillDokumentRequest request) throws BrevException {
		BrevStatusVO brevStatus = request.getBrevStatus();
		BrevVO redBrev = request.getBrev();
		BrevVO pdfBrev = request.getPdfBrev();

		brevStatus.setStatus(Konstanter.BREVSTATUS_FERDIG);
		brevStatus.setSkrivertype(Konstanter.SKRIVERTYPE_INGEN);
		redBrev.setLagerStatus(Konstanter.BREVLAGER_STATUS_KLADD);
		pdfBrev.setLagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG);
		verifyChangeRequest(brevStatus);
		brevstatusService.lagreBrevStatus(voTilBrevstatusConverter.convert(brevStatus), brevStatus.getToken());
		if (brevStatus.getSystemID().startsWith(SystemType.PE.toString())) {
			dokarkivConsumer.lagreFerdigstiltDokument(brevStatus.getBrevreferanse(), redBrev, pdfBrev);
		} else {
			brevferdigstillBrevlagerDokument(brevStatus, redBrev, pdfBrev);
		}
		//FIXME:
		//sendKvittering(pdfBrevVO, brevStatus, systemType, brevStatus.getReturKoe());
	}

	private void brevferdigstillBrevlagerDokument(BrevStatusVO brevStatus, BrevVO redBrev, BrevVO pdfBrev) {
		translateContentTypeDocxToDb2(redBrev);
		Brev brev = voTilBrevConverter.convert(redBrev);
		defaultBrevlagerHistorikkService.insertHistorikk(brev);
		brevRepository.save(brev);
	}

	@Override
	public void lagreDokument(LagreDokumentRequest request) throws BrevException {
		request.validate();
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
		ArgumentValidator.isNotNull(brevStatus);
		verifyChangeRequest(brevStatus);
		brevStatus.setStatus(Konstanter.BREVSTATUS_AVBRUTT);
		brevstatusService.lagreBrevStatus(voTilBrevstatusConverter.convert(brevStatus), brevStatus.getToken());

		if (brevStatus.getReturKoe() != null) {
			KvitteringVO kvittering = new KvitteringVO();
			kvittering.setSystemID(brevStatus.getSystemID());
			kvittering.setBrevreferanse(brevStatus.getBrevreferanse());
			brevStatus.setStatus(Konstanter.BREVSTATUS_AVBRUTT);

			XMLService service = XMLServiceFactory.getInstance().createXMLService();
			String xmlKvittering = service.unmarshal(kvittering, brevStatus);
			biMessageProducer.sendReturMelding(brevStatus.getReturKoe(), false, null, xmlKvittering);
		}
		log.info("Brevet ble avbrutt");
	}

	@Override
	public BrevStatusVO hentBrevStatus(String systemId, String brevreferanse) throws BrevTechnicalException {
		Brevstatus brevstatus = brevstatusService.hentBrevStatus(systemId, brevreferanse);
		if(brevstatus!=null){
			BrevStatusVO brevStatusVO = brevstatusTilVoConverter.convert(brevstatus);
			brevStatusVO.setKnappStatus(knappStatusUtil.getKnappStatus(brevStatusVO.getBrevmal()));
			return brevStatusVO;
		}
		return null;
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
		Brevstatus gmlStatus = brevstatusService.lagreBrevStatus(brevstatus, token);
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
			defaultBrevlagerHistorikkService.insertHistorikk(brevList.get(0));
			return true;
		} else {
			return false;
		}
	}

	private void lagreJoarkDokument(BrevVO brev, BrevStatusVO brevstatus, SystemType systemType, String returKoe)
			throws BrevTechnicalException {
		dokarkivConsumer.lagreDokument(brevstatus.getBrevreferanse(), brev.getContentType(), brev.getBrevdata());
	}

	protected void verifyChangeRequest(BrevStatusVO brevStatus) throws BrevException {
		checkRequiredFields(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken());
		Brevstatus oldBrevStatus = brevstatusService.hentBrevStatus(brevStatus.getSystemID(), brevStatus.getBrevreferanse());
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
		return brevtilgangService.sjekkTilgang(systemID, brevreferanse, token);
	}

	private BrevVO hentDokumentFraJOARK(String brevreferanse) throws
			BrevTechnicalException, BrevFunctionalException {
		BrevVO result = safConsumer.hentDokument(brevreferanse);

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

}
