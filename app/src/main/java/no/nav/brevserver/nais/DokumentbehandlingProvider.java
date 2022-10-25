package no.nav.brevserver.nais;

import no.nav.brevserver.app.dokumentbehandling.to.HentDokumentResponse;
import no.nav.brevserver.core.constants.KnappStatus;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.exception.BrevFinnesIkkeException;
import no.nav.brevserver.nais.support.AvbrytDokumentRequestMapper;
import no.nav.brevserver.nais.support.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.nais.support.HentDokumentRequestMapper;
import no.nav.brevserver.nais.support.HentDokumentResponseMapper;
import no.nav.brevserver.nais.support.LagreDokumentRequestMapper;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.exception.BrevRuntimeException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.nais.support.AvbrytDokumentRequestMapper;
import no.nav.brevserver.nais.support.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.nais.support.HentDokumentRequestMapper;
import no.nav.brevserver.nais.support.HentDokumentResponseMapper;
import no.nav.brevserver.nais.support.LagreDokumentRequestMapper;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.springframework.stereotype.Service;

/**
 * Provider that maps from and to the Dokumentbehandling webservice model and delegates to Service layer implementations.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Service
public class DokumentbehandlingProvider {

	private final BrevlagerService brevlagerService;
	private final BrevstatusService brevstatusService;
	private final LagreDokumentRequestMapper lagreDokumentRequestMapper;
	private final AvbrytDokumentRequestMapper avbrytDokumentRequestMapper;
	private final FerdigstillDokumentRequestMapper ferdigstillDokumentRequestMapper;
	private final HentDokumentRequestMapper hentDokumentRequestMapper;
	private final HentDokumentResponseMapper hentDokumentResponseMapper;

	public DokumentbehandlingProvider(BrevlagerService brevlagerService,
									  BrevstatusService brevstatusService,
									  LagreDokumentRequestMapper lagreDokumentRequestMapper,
									  AvbrytDokumentRequestMapper avbrytDokumentRequestMapper,
									  FerdigstillDokumentRequestMapper ferdigstillDokumentRequestMapper,
									  HentDokumentRequestMapper hentDokumentRequestMapper,
									  HentDokumentResponseMapper hentDokumentResponseMapper){
		this.brevlagerService = brevlagerService;
		this.brevstatusService = brevstatusService;
		this.lagreDokumentRequestMapper = lagreDokumentRequestMapper;
		this.avbrytDokumentRequestMapper = avbrytDokumentRequestMapper;
		this.ferdigstillDokumentRequestMapper = ferdigstillDokumentRequestMapper;
		this.hentDokumentRequestMapper = hentDokumentRequestMapper;
		this.hentDokumentResponseMapper = hentDokumentResponseMapper;
	}

	public HentDokumentResponse2 hentDokument(HentDokumentRequest request) throws BrevTechnicalException, BrevFunctionalException, BrevFinnesIkkeException {
		no.nav.brevserver.app.dokumentbehandling.to.HentDokumentRequest hentDokumentRequest = hentDokumentRequestMapper.map(request);
		hentDokumentRequest.validate();
		BrevStatusVO brevStatus = hentDokumentRequest.getBrevStatus();
		BrevVO brev = brevlagerService.hentDokumentFromBrevlagerOrJoark(brevStatus);
		if (brev == null) {
			throw new BrevFinnesIkkeException("Brevserver fant ikke dokumentet med brevreferanse: "
					+ brevStatus.getBrevreferanse());
		}
		return hentDokumentResponseMapper.map(createResponse(brevStatus, brev));
	}

	private HentDokumentResponse createResponse(BrevStatusVO brevStatus, BrevVO brev) {
		HentDokumentResponse response = new HentDokumentResponse();
		response.setContentType(brev.getContentType());
		response.setDokumentData(brev.getBrevdata());
		response.setKnappStatus(hentKnappStatus(brevStatus.getSystemID(), brevStatus.getBrevreferanse()).toString());
		return response;
	}

	public KnappStatus hentKnappStatus(String systemId, String brevreferanse) {
		try {
			BrevStatusVO result = brevstatusService.hentBrevStatus(brevreferanse, systemId);
			if (result == null) {
				return KnappStatus.getDefault();
			}
			return result.getKnappStatus();
		} catch (BrevException e) {
			throw new BrevRuntimeException(e.getMessage(), e);
		}
	}

	public void lagreDokument(LagreDokumentRequest lagreDokumentRequest) throws BrevException {
		no.nav.brevserver.app.dokumentbehandling.to.LagreDokumentRequest request = lagreDokumentRequestMapper.map(lagreDokumentRequest);
		request.validate();
		BrevVO brev = request.getBrev();
		BrevStatusVO brevStatus = request.getBrevStatus();
		SystemType systemType = brevStatus.getSystemID().startsWith("PE") ? SystemType.PE : SystemType.BI;
		brev.setLagerStatus(Konstanter.BREVLAGER_STATUS_KLADD);
		brevStatus.setStatus(Konstanter.BREVSTATUS_LAGRET_KLADD);
		brevlagerService.lagreDokument(brev, brevStatus, systemType);

	}

		public void avbrytDokument(AvbrytDokumentRequest avbrytDokumentRequest) throws BrevException {
			no.nav.brevserver.app.dokumentbehandling.to.AvbrytDokumentRequest request = avbrytDokumentRequestMapper.map(avbrytDokumentRequest);
			request.validate();
			BrevStatusVO brevStatus = request.getBrevStatus();
			brevlagerService.avbrytDokument(brevStatus);
		}

		public void ferdigstillDokument(FerdigstillDokumentRequest ferdigstillDokumentRequest) throws BrevException {
			no.nav.brevserver.app.dokumentbehandling.to.FerdigstillDokumentRequest request = ferdigstillDokumentRequestMapper.map(ferdigstillDokumentRequest);
			request.validate();
			brevlagerService.ferdigstillBrev(request.getBrevStatus(), request.getBrev(), request.getPdfBrev());
		}

		public void ping(PingRequest pingRequest) {
		brevlagerService.ping();
	}
}
