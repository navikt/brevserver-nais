package no.nav.brevserver.service.dokumentbehandling.support;

import no.nav.brevserver.controller.ControllerBeanFactory;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.service.dokumentbehandling.LagreDokumentService;
import no.nav.brevserver.service.dokumentbehandling.to.LagreDokumentRequest;

/**
 * Default implementation of LagreDokumentService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultLagreDokumentService implements LagreDokumentService {

	@Override
	public void lagreDokument(LagreDokumentRequest request) {
		request.validate();
		performLagreDokument(request);
	}

	private void performLagreDokument(LagreDokumentRequest request) {
		BrevVO brev = request.getBrev();
		BrevStatusVO brevStatus = request.getBrevStatus();

		SystemType systemType = brevStatus.getSystemID().startsWith("PE") ? SystemType.PE : SystemType.BI;

		brev.setLagerStatus(Konstanter.BREVLAGER_STATUS_KLADD);
		brevStatus.setStatus(Konstanter.BREVSTATUS_LAGRET_KLADD);

		ControllerBeanFactory.getInstance().getController().lagreDokument(brev, brevStatus, systemType);
	}
}
