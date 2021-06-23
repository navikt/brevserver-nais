package no.nav.brevserver.service.dokumentbehandling.support;

import no.nav.brevserver.service.dokumentbehandling.AvbrytDokumentService;
import no.nav.brevserver.service.dokumentbehandling.DokumentbehandlingService;
import no.nav.brevserver.service.dokumentbehandling.FerdigstillDokumentService;
import no.nav.brevserver.service.dokumentbehandling.HentDokumentService;
import no.nav.brevserver.service.dokumentbehandling.LagreDokumentService;
import no.nav.brevserver.service.dokumentbehandling.PingService;
import no.nav.brevserver.service.dokumentbehandling.to.AvbrytDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentResponse;
import no.nav.brevserver.service.dokumentbehandling.to.LagreDokumentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of DokumentbehandlingService
 * Delegates to subservices for the different operations
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Service
public class DefaultDokumentbehandlingService implements DokumentbehandlingService {
	private HentDokumentService hentDokumentService;
	private LagreDokumentService lagreDokumentService;
	private AvbrytDokumentService avbrytDokumentService;
	private FerdigstillDokumentService ferdigstillDokumentService;
	private PingService pingService;

	@Autowired
	public DefaultDokumentbehandlingService(HentDokumentService hentDokumentService) {
		this.hentDokumentService = hentDokumentService;
		lagreDokumentService = new DefaultLagreDokumentService();
		avbrytDokumentService = new DefaultAvbrytDokumentService();
		ferdigstillDokumentService = new DefaultFerdigstillDokumentService();
		pingService = new DefaultPingService();
	}

	@Override
	public HentDokumentResponse hentDokument(HentDokumentRequest hentDokumentRequest) {
		return hentDokumentService.hentDokument(hentDokumentRequest);
	}

	@Override
	public void lagreDokument(LagreDokumentRequest lagreDokumentRequest) {
		lagreDokumentService.lagreDokument(lagreDokumentRequest);
	}

	@Override
	public void avbrytDokument(AvbrytDokumentRequest avbrytDokumentRequest) {
		avbrytDokumentService.avbrytDokument(avbrytDokumentRequest);
	}

	@Override
	public void ferdigstillDokument(FerdigstillDokumentRequest ferdigstillDokumentRequest) {
		ferdigstillDokumentService.ferdigstillDokument(ferdigstillDokumentRequest);
	}

	@Override
	public void ping() {
		pingService.ping();
	}
}
