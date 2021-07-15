package no.nav.brevserver.service.dokumentbehandling.support;

import no.nav.brevserver.controller.ControllerBeanFactory;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.service.dokumentbehandling.FerdigstillDokumentService;
import no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest;

/**
 * Default implementation of FerdigstillDokumentService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultFerdigstillDokumentService implements FerdigstillDokumentService {

	@Override
	public void ferdigstillDokument(FerdigstillDokumentRequest request) {
		request.validate();
		performFerdigstillDokument(request);
	}

	private void performFerdigstillDokument(FerdigstillDokumentRequest request) {
		BrevStatusVO brevStatus = request.getBrevStatus();

		SystemType type = brevStatus.getSystemID().startsWith("PE") ? SystemType.PE : SystemType.BI;
		BrevVO redBrev = request.getBrev();
		BrevVO pdfBrev = request.getPdfBrev();

		brevStatus.setStatus(Konstanter.BREVSTATUS_FERDIG);
		brevStatus.setSkrivertype(Konstanter.SKRIVERTYPE_INGEN);
		redBrev.setLagerStatus(Konstanter.BREVLAGER_STATUS_KLADD);
		pdfBrev.setLagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG);

		ControllerBeanFactory.getInstance().getController().ferdigstillDokument(brevStatus, redBrev, pdfBrev, type);
	}
}
