package no.nav.brevserver.provider.support;

import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevserverServiceBean;
import no.nav.brevserver.service.dokumentbehandling.DokumentbehandlingService;
import no.nav.brevserver.service.dokumentbehandling.support.DefaultDokumentbehandlingService;
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
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.DokumentbehandlingPortType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provider that maps from and to the Dokumentbehandling webservice model and delegates to Service layer implementations.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Service
public class DokumentbehandlingProvider implements DokumentbehandlingPortType {

	private final BrevlagerService brevlagerService;
	private final HentDokumentRequestMapper hentDokumentRequestMapper;
	private final HentDokumentResponseMapper hentDokumentResponseMapper;
	private final LagreDokumentRequestMapper lagreDokumentRequestMapper;
	private final AvbrytDokumentRequestMapper avbrytDokumentRequestMapper;
	private final FerdigstillDokumentRequestMapper ferdigstillDokumentRequestMapper;

	@Autowired
	public DokumentbehandlingProvider() {
	}

	@Override
	public HentDokumentResponse2 hentDokument(HentDokumentRequest hentDokumentRequest) {
		return hentDokumentResponseMapper.map(
				brevlagerService.hentDokumentFromBrevlagerOrJoark(hentDokumentRequestMapper.map(hentDokumentRequest)));
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
