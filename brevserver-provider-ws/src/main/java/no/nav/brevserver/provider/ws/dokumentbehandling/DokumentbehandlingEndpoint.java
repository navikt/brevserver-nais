package no.nav.brevserver.provider.ws.dokumentbehandling;

import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import no.nav.brevserver.provider.support.DokumentbehandlingProvider;
import no.nav.brevserver.server.common.exception.BrevSecurityException;
import no.nav.brevserver.server.common.log.Log;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.DokumentbehandlingPortType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;

/**
 * Implementation of the JAX-WS generated service interface DokumentbehandlingPortType.
 * Delegates to DokumentbehandlingProvider at the provider layer.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@MTOM
@WebService(targetNamespace = "http://dokumentbehandling.brevogarkiv.tjenester.nav.no/",
		endpointInterface = "no.nav.tjenester.brevogarkiv.dokumentbehandling.DokumentbehandlingPortType",
		serviceName = "Dokumentbehandling",
		portName = "DokumentbehandlingPort")
public class DokumentbehandlingEndpoint implements DokumentbehandlingPortType {

	private static final Log log = new Log(DokumentbehandlingEndpoint.class);
	private static final String EXCEPTION_MESSAGE = "SOAPkall feilet";

	private DokumentbehandlingProvider dokumentbehandlingProvider;

	public DokumentbehandlingEndpoint() {
		dokumentbehandlingProvider = new DokumentbehandlingProvider();
	}

	@Override
	public HentDokumentResponse2 hentDokument(HentDokumentRequest hentDokumentRequest) {
		try {
			return dokumentbehandlingProvider.hentDokument(hentDokumentRequest);
		} catch (RuntimeException e) {
			if (e.getCause() != null && e.getCause() instanceof BrevSecurityException) {
				throw e;
			}
			log.error("hentDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}

	@Override
	public void lagreDokument(LagreDokumentRequest lagreDokumentRequest) {
		try {
			dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest);
		} catch (RuntimeException e) {
			log.error("lagreDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}

	@Override
	public void avbrytDokument(AvbrytDokumentRequest avbrytDokumentRequest) {
		try {
			dokumentbehandlingProvider.avbrytDokument(avbrytDokumentRequest);
		} catch (RuntimeException e) {
			log.error("avbrytDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}

	@Override
	public void ferdigstillDokument(FerdigstillDokumentRequest ferdigstillDokumentRequest) {
		try {
			dokumentbehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);
		} catch (RuntimeException e) {
			log.error("ferdigstillDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}

	@Override
	public void ping(PingRequest pingRequest) {
		try {
			dokumentbehandlingProvider.ping(pingRequest);
		} catch (RuntimeException e) {
			log.error("ping", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}
}
