package no.nav.brevserver.service.dokumentbehandling.support;


import no.nav.brevserver.controller.ControllerBeanFactory;
import no.nav.brevserver.controller.ControllerBi;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.service.dokumentbehandling.AvbrytDokumentService;
import no.nav.brevserver.service.dokumentbehandling.to.AvbrytDokumentRequest;

/**
 * Default implementation of AvbrytDokumentService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultAvbrytDokumentService implements AvbrytDokumentService {

	@Override
	public void avbrytDokument(AvbrytDokumentRequest request) {
		validateRequest(request);
		performAvbrytDokument(request);
	}

	private void validateRequest(AvbrytDokumentRequest request) {
		request.validate();
	}

	private void performAvbrytDokument(AvbrytDokumentRequest request) {
		BrevStatusVO brevStatus = request.getBrevStatus();

		ControllerBi controller = ControllerBeanFactory.getInstance().getController();
		controller.avbrytDokument(brevStatus);
	}
}
