package no.nav.brevserver.service.dokumentbehandling.support;

import no.nav.brevserver.controller.ControllerBeanFactory;
import no.nav.brevserver.controller.ControllerBi;
import no.nav.brevserver.server.common.exception.BrevRuntimeException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.service.dokumentbehandling.HentDokumentService;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentResponse;

/**
 * Default implementation of HentDokumentService
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultHentDokumentService implements HentDokumentService {

	private ControllerBi controller;

	@Override
	public HentDokumentResponse hentDokument(HentDokumentRequest request) {
		validateRequest(request);
		return performHentDokument(request);
	}

	private void validateRequest(HentDokumentRequest request) {
		request.validate();
	}

	private HentDokumentResponse performHentDokument(HentDokumentRequest request) {
		BrevStatusVO brevStatus = request.getBrevStatus();

		controller = ControllerBeanFactory.getInstance().getController();
		BrevVO brev = hentDokumentFromBrevlagerOrJoark(brevStatus);
		
		return createResponse(brevStatus, brev);
	}

	private BrevVO hentDokumentFromBrevlagerOrJoark(BrevStatusVO brevStatus) {
		BrevVO brev = controller.hentDokument(brevStatus);
		if (brev == null) {
			throw new BrevRuntimeException("Brevserver fant ikke dokumentet med brevreferanse: "
					+ brevStatus.getBrevreferanse());
		}
		return brev;
	}

	private HentDokumentResponse createResponse(BrevStatusVO brevStatus, BrevVO brev) {
		HentDokumentResponse response = new HentDokumentResponse();
		response.setContentType(brev.getContentType());
		response.setDokumentData(brev.getBrevdata());
		response.setKnappStatus(controller.hentKnappStatus(brevStatus.getSystemID(), brevStatus.getBrevreferanse()).toString());
		return response;
	}
}
