package no.nav.brevserver.nais;

import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.service.dokumentbehandling.DokumentbehandlingService;
import no.nav.brevserver.service.dokumentbehandling.support.DefaultDokumentbehandlingService;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentResponse;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.springframework.stereotype.Service;

/**
 * Provider that maps from and to the Dokumentbehandling webservice model and delegates to Service layer implementations.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Service
public class DokumentbehandlingProvider {

	private DokumentbehandlingService dokumentbehandlingService;

	public DokumentbehandlingProvider() {
		dokumentbehandlingService = new DefaultDokumentbehandlingService();
	}

	public HentDokumentResponse hentDokument() {
		HentDokumentRequest req = new HentDokumentRequest();
		BrevStatusVO v0 = new BrevStatusVO();
		v0.setStatus("FERDIG");
		v0.setSystemID("FS10");
		v0.setToken("1093");
		v0.setBrevreferanse("1096");
		req.setBrevStatus(v0);
		return dokumentbehandlingService.hentDokument(req);
	}
/*
	@Override
	public void lagreDokument(LagreDokumentRequest lagreDokumentRequest) {
		String systemIdKey = "systemId";
		String brevreferanseKey = "brevreferanse";
		try {
			MDC.put(systemIdKey, lagreDokumentRequest.getSystemId());
			MDC.put(brevreferanseKey, lagreDokumentRequest.getBrevreferanse());
			
			dokumentbehandlingService.lagreDokument(lagreDokumentRequestMapper.map(lagreDokumentRequest));
		} finally {
			MDC.remove(systemIdKey);
			MDC.remove(brevreferanseKey);
		}
	}

	@Override
	public void avbrytDokument(AvbrytDokumentRequest avbrytDokumentRequest) {
		dokumentbehandlingService.avbrytDokument(avbrytDokumentRequestMapper.map(avbrytDokumentRequest));
	}

	@Override
	public void ferdigstillDokument(FerdigstillDokumentRequest ferdigstillDokumentRequest) {
		dokumentbehandlingService.ferdigstillDokument(ferdigstillDokumentRequestMapper.map(ferdigstillDokumentRequest));
	}
*/
	public void ping(PingRequest pingRequest) {
		dokumentbehandlingService.ping();
	}
}
