package no.nav.brevserver.nais;

import no.nav.brevserver.nais.support.AvbrytDokumentRequestMapper;
import no.nav.brevserver.nais.support.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.nais.support.LagreDokumentRequestMapper;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentResponse;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provider that maps from and to the Dokumentbehandling webservice model and delegates to Service layer implementations.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Service
public class DokumentbehandlingProvider {

	private final BrevlagerService brevlagerService;
	private final LagreDokumentRequestMapper lagreDokumentRequestMapper;
	private final AvbrytDokumentRequestMapper avbrytDokumentRequestMapper;
	private final FerdigstillDokumentRequestMapper ferdigstillDokumentRequestMapper;

	@Autowired
	public DokumentbehandlingProvider(BrevlagerService brevlagerService,
									  LagreDokumentRequestMapper lagreDokumentRequestMapper,
									  AvbrytDokumentRequestMapper avbrytDokumentRequestMapper,
									  FerdigstillDokumentRequestMapper ferdigstillDokumentRequestMapper){
		this.brevlagerService = brevlagerService;
		this.lagreDokumentRequestMapper = lagreDokumentRequestMapper;
		this.avbrytDokumentRequestMapper = avbrytDokumentRequestMapper;
		this.ferdigstillDokumentRequestMapper = ferdigstillDokumentRequestMapper;
	}

	public HentDokumentResponse hentDokument(HentDokumentRequest request) throws BrevTechnicalException, BrevFunctionalException {
		BrevStatusVO brevStatus = request.getBrevStatus();
		BrevVO brev = brevlagerService.hentDokumentFromBrevlagerOrJoark(brevStatus);
		return createResponse(brevStatus, brev);
	}

	private HentDokumentResponse createResponse(BrevStatusVO brevStatus, BrevVO brev) {
		HentDokumentResponse response = new HentDokumentResponse();
		response.setContentType(brev.getContentType());
		response.setDokumentData(brev.getBrevdata());
		//response.setKnappStatus(controller.hentKnappStatus(brevStatus.getSystemID(), brevStatus.getBrevreferanse()).toString());
		return response;
	}


	public void lagreDokument(LagreDokumentRequest lagreDokumentRequest) throws BrevException {
		String systemIdKey = "systemId";
		String brevreferanseKey = "brevreferanse";
		try {
			MDC.put(systemIdKey, lagreDokumentRequest.getSystemId());
			MDC.put(brevreferanseKey, lagreDokumentRequest.getBrevreferanse());

			brevlagerService.lagreDokument(lagreDokumentRequestMapper.map(lagreDokumentRequest));
		} finally {
			MDC.remove(systemIdKey);
			MDC.remove(brevreferanseKey);
		}
	}

		public void avbrytDokument(AvbrytDokumentRequest avbrytDokumentRequest) throws BrevException {
			brevlagerService.avbrytDokument(avbrytDokumentRequestMapper.map(avbrytDokumentRequest));
		}

		public void ferdigstillDokument(FerdigstillDokumentRequest ferdigstillDokumentRequest) throws BrevException {
			brevlagerService.ferdigstillDokument(ferdigstillDokumentRequestMapper.map(ferdigstillDokumentRequest));
		}

	public void ping(PingRequest pingRequest) {
		brevlagerService.ping();
	}
}
