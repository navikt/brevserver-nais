package no.nav.brevserver.service.dokumentbehandling.support;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevFinnesAlleredeException;
import no.nav.brevserver.core.exception.BrevFinnesIkkeException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.exception.BrevserverFunctionalException;
import no.nav.brevserver.core.exception.BrevserverTechnicalException;
import no.nav.brevserver.core.exception.InputValideringFeiletException;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static no.nav.brevserver.core.constants.Konstanter.BREVLAGER_STATUS_FERDIG;
import static no.nav.brevserver.core.constants.Konstanter.BREVLAGER_STATUS_KLADD;
import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_AVBRUTT;
import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_FERDIG;
import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_UTSKRIFT;
import static no.nav.brevserver.core.constants.Konstanter.SKRIVERTYPE_INGEN;
import static no.nav.brevserver.core.constants.SystemType.BI;
import static no.nav.brevserver.core.constants.SystemType.PE;
import static no.nav.brevserver.core.exception.BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG;
import static no.nav.brevserver.core.utils.SafeLoggingUtil.sanitizeUnsafeChar;

@Service
@Transactional
@Slf4j
public class DefaultBrevlagerService implements BrevlagerService {

	private final JoarkService dokarkivService;
	private final BrevstatusService brevstatusService;
	private final BrevRepository brevRepository;
	private final DefaultBrevlagerHistorikkService defaultBrevlagerHistorikkService;
	private final BrevTilVoConverter brevTilVoConverter;
	private final VoTilBrevConverter voTilBrevConverter;
	private final BrevtilgangService brevtilgangService;
	private final KvitteringService kvitteringService;

	public DefaultBrevlagerService(@Qualifier("dokarkivService") JoarkService dokarkivService,
								   BrevTilVoConverter brevTilVoConverter,
								   BrevstatusService brevstatusService,
								   BrevRepository brevRepository,
								   VoTilBrevConverter voTilBrevConverter,
								   DefaultBrevlagerHistorikkService defaultBrevlagerHistorikkService,
								   BrevtilgangService brevtilgangService,
								   KvitteringService kvitteringService) {
		this.dokarkivService = dokarkivService;
		this.brevRepository = brevRepository;
		this.brevTilVoConverter = brevTilVoConverter;
		this.defaultBrevlagerHistorikkService = defaultBrevlagerHistorikkService;
		this.voTilBrevConverter = voTilBrevConverter;
		this.brevtilgangService = brevtilgangService;
		this.kvitteringService = kvitteringService;
		this.brevstatusService = brevstatusService;
	}


	@Override
	public BrevVO getBrev(String systemId, String brevreferanse) throws BrevTechnicalException {
		try {
			Optional<Brev> brevOpt = brevRepository.findById(new BrevreferanseSystemCompositeId(brevreferanse, systemId));
			if (brevOpt.isPresent()) {
				return brevTilVoConverter.convert(brevOpt.get());
			} else {
				throw new BrevFinnesIkkeException(brevreferanse);
			}
		} catch (RuntimeException e) {
			throw new BrevserverTechnicalException("Feil i kontakt med databasen. Forsøk på nytt senere", e);
		}
	}

	@Override
	public BrevStatusVO lagreBrev(BrevVO brev, BrevStatusVO brevstatus) throws BrevTechnicalException {
		try {
			backupIfExistingBrev(brev.getBrevreferanse(), brev.getSystemID());
			BrevStatusVO gmlStatus = brevstatusService.lagreBrevStatus(brevstatus);

			if (brevstatus.getSystemID() != null && brevstatus.getSystemID().startsWith(PE.toString())) {
				dokarkivService.lagreDokument(brevstatus.getBrevreferanse(), brev.getContentType(), brev.getBrevdata());
			} else {
				brevRepository.save(voTilBrevConverter.convert(brev));
			}

			return gmlStatus;
		} catch(BrevserverFunctionalException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new BrevTechnicalException(DATABASE_IKKE_TILGJENGELIG, e);
		}
	}

	@Override
	public void ferdigstillBrev(BrevStatusVO brevStatus, BrevVO redBrev, BrevVO pdfBrev) throws BrevException {
		try {
			log.info("Ferdigstille brevreferanse={}, systemId={}, mal={}", sanitizeUnsafeChar(brevStatus.getBrevreferanse()), sanitizeUnsafeChar(brevStatus.getSystemID()), brevStatus.getBrevmal());

			verifyChangeRequest(brevStatus);
			brevStatus.setStatus(BREVSTATUS_FERDIG);
			brevStatus.setSkrivertype(SKRIVERTYPE_INGEN);
			redBrev.setLagerStatus(BREVLAGER_STATUS_KLADD);
			pdfBrev.setLagerStatus(BREVLAGER_STATUS_FERDIG);

			if (brevStatus.getSystemID().startsWith(PE.toString())) {
				dokarkivService.lagreFerdigstiltDokument(brevStatus.getBrevreferanse(), redBrev, pdfBrev);
			} else {
				brevferdigstillBrevlagerDokument(brevStatus, redBrev, pdfBrev);
			}

			brevstatusService.lagreBrevStatus(brevStatus);

			log.info("Ferdigstilte brev {} fra {} mal:{}", sanitizeUnsafeChar(brevStatus.getBrevreferanse()), sanitizeUnsafeChar(brevStatus.getSystemID()), brevStatus.getBrevmal());
			SystemType systemType = brevStatus.getSystemID().startsWith("PE") ? PE : BI;
			kvitteringService.sendKvittering(pdfBrev, brevStatus, systemType, brevStatus.getReturKoe());
		} catch (RuntimeException e) {
			throw new BrevserverTechnicalException("Teknisk feil. Forsøk på nytt senere", e);
		}
	}

	@Override
	public BrevVO hentDokumentFromBrevlagerOrJoark(BrevStatusVO brevStatus) throws BrevTechnicalException {
		checkRequiredFields(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken());
		log.info("Henter dokument med brevreferanse={}, systemID={}", sanitizeUnsafeChar(brevStatus.getBrevreferanse()), sanitizeUnsafeChar(brevStatus.getSystemID()));

		BrevVO result = null;
		if (sjekkSystemTokenTilgang(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken())) {
			if (brevStatus.getSystemID().startsWith(PE.toString())) {
				result = hentDokumentFraJOARK(brevStatus.getBrevreferanse());
				log.info("Hentet dokument med brevreferanse={}, systemID={}, contentType={} fra joark",
						sanitizeUnsafeChar(brevStatus.getBrevreferanse()), sanitizeUnsafeChar(brevStatus.getSystemID()), result.getContentType());
			} else {
				result = getBrev(brevStatus.getSystemID(), brevStatus.getBrevreferanse());
				log.info("Hentet dokument med brevreferanse={}, systemID={}, contentType={} fra brevlageret",
						sanitizeUnsafeChar(brevStatus.getBrevreferanse()), sanitizeUnsafeChar(brevStatus.getSystemID()), result.getContentType());
			}
		} else {
			log.warn("Bruker har ikke tilgang til brev med brevreferanse={}, systemID={}", sanitizeUnsafeChar(brevStatus.getBrevreferanse()), brevStatus.getSystemID());
		}

		return result;
	}

	private void brevferdigstillBrevlagerDokument(BrevStatusVO brevStatus, BrevVO redBrevVo, BrevVO pdfBrevVo) throws BrevTechnicalException {
		log.info("Prøver å ferdigstille brevlagerdokument {} fra {} mal:{}", sanitizeUnsafeChar(brevStatus.getBrevreferanse()), sanitizeUnsafeChar(brevStatus.getSystemID()), brevStatus.getBrevmal());

		Brev redBrev = voTilBrevConverter.convert(redBrevVo);
		Brev pdfBrev = voTilBrevConverter.convert(pdfBrevVo);
		backupIfExistingBrev(pdfBrevVo.getBrevreferanse(), pdfBrevVo.getSystemID());
		defaultBrevlagerHistorikkService.insertHistorikk(redBrev);
		brevRepository.save(pdfBrev);

		log.info("brevlagerdokument {} fra {} har blitt ferdigstilt", sanitizeUnsafeChar(brevStatus.getBrevreferanse()), sanitizeUnsafeChar(brevStatus.getSystemID()));
	}

	@Override
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

		log.info("Brevet ble avbrutt. Brevref:{}", sanitizeUnsafeChar(brevStatus.getBrevreferanse()));
	}

	@Override
	public void lagreDokument(BrevVO brev, BrevStatusVO brevStatusVO, SystemType systemType) throws BrevException {
		log.info("Lagrer dokument {} fra {} mal:{}", sanitizeUnsafeChar(brevStatusVO.getBrevreferanse()), sanitizeUnsafeChar(brevStatusVO.getSystemID()), sanitizeUnsafeChar(brevStatusVO.getBrevmal()));
		if (brev == null || brevStatusVO == null) {
			throw new IllegalArgumentException("Brevstatus er null!");
		}

		verifyChangeRequest(brevStatusVO);
		if (brevStatusVO.getSystemID().startsWith(PE.toString())) {
			dokarkivService.lagreDokument(brev.getBrevreferanse(), brev.getContentType(), brev.getBrevdata());
		} else {
			lagreBrev(brev, brevStatusVO);
		}

		kvitteringService.sendKvittering(brev, brevStatusVO, systemType, brevStatusVO.getReturKoe());
	}


	private void backupIfExistingBrev(String brevreferanse, String systemId) {
		Optional<Brev> brevOpt = brevRepository.findById(new BrevreferanseSystemCompositeId(brevreferanse, systemId));

		if (brevOpt.isPresent()) {
			Brev brev = brevOpt.get();
			if (brev.getStatus().equals(BREVLAGER_STATUS_FERDIG)) {
				throw new BrevserverFunctionalException("Brevet har status = '" + BREVLAGER_STATUS_FERDIG + "' og kan ikke endres");
			}
			defaultBrevlagerHistorikkService.insertHistorikk(brev);
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

	private BrevVO hentDokumentFraJOARK(String journalpostId) {
		return dokarkivService.hentDokument(journalpostId);
	}

	protected void checkRequiredFields(String systemId, String brevreferanse, String token) throws BrevTechnicalException {
		if (systemId == null) {
			throw new InputValideringFeiletException("Manglende obligatorisk felt: systemId");
		} else if (brevreferanse == null) {
			throw new InputValideringFeiletException("Manglende obligatorisk felt: brevreferanse");
		} else if (token == null) {
			throw new InputValideringFeiletException("Manglende obligatorisk felt: token");
		}
	}

}
