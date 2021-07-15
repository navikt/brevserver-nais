package no.nav.brevserver.provider.support;

import org.slf4j.MDC;

import no.nav.brevserver.provider.map.AvbrytDokumentRequestMapper;
import no.nav.brevserver.provider.map.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.provider.map.HentDokumentRequestMapper;
import no.nav.brevserver.provider.map.HentDokumentResponseMapper;
import no.nav.brevserver.provider.map.LagreDokumentRequestMapper;
import no.nav.brevserver.provider.map.support.DefaultAvbrytDokumentRequestMapper;
import no.nav.brevserver.provider.map.support.DefaultFerdigstillDokumentRequestMapper;
import no.nav.brevserver.provider.map.support.DefaultHentDokumentRequestMapper;
import no.nav.brevserver.provider.map.support.DefaultHentDokumentResponseMapper;
import no.nav.brevserver.provider.map.support.DefaultLagreDokumentRequestMapper;
import no.nav.brevserver.service.dokumentbehandling.DokumentbehandlingService;
import no.nav.brevserver.service.dokumentbehandling.support.DefaultDokumentbehandlingService;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.DokumentbehandlingPortType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;

/**
 * Provider that maps from and to the Dokumentbehandling webservice model and delegates to Service layer implementations.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DokumentbehandlingProvider implements DokumentbehandlingPortType {

	private DokumentbehandlingService dokumentbehandlingService;

	private HentDokumentRequestMapper hentDokumentRequestMapper;
	private HentDokumentResponseMapper hentDokumentResponseMapper;
	private LagreDokumentRequestMapper lagreDokumentRequestMapper;
	private AvbrytDokumentRequestMapper avbrytDokumentRequestMapper;
	private FerdigstillDokumentRequestMapper ferdigstillDokumentRequestMapper;

	public DokumentbehandlingProvider() {
		dokumentbehandlingService = new DefaultDokumentbehandlingService();

		hentDokumentRequestMapper = new DefaultHentDokumentRequestMapper();
		hentDokumentResponseMapper = new DefaultHentDokumentResponseMapper();
		lagreDokumentRequestMapper = new DefaultLagreDokumentRequestMapper();
		avbrytDokumentRequestMapper = new DefaultAvbrytDokumentRequestMapper();
		ferdigstillDokumentRequestMapper = new DefaultFerdigstillDokumentRequestMapper();
	}

	@Override
	public HentDokumentResponse2 hentDokument(HentDokumentRequest hentDokumentRequest) {
		return hentDokumentResponseMapper.map(
				dokumentbehandlingService.hentDokument(hentDokumentRequestMapper.map(hentDokumentRequest)));
	}

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

	@Override
	public void ping(PingRequest pingRequest) {
		dokumentbehandlingService.ping();
	}
}
