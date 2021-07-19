package no.nav.brevserver.ws.dokumentbehandling;

import no.nav.brevserver.nais.DokumentbehandlingProvider;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevSecurityException;
import no.nav.brevserver.server.common.log.Log;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.DokumentbehandlingPortType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * Implementation of the JAX-WS generated service interface DokumentbehandlingPortType.
 * Delegates to DokumentbehandlingProvider at the provider layer.
 *
 * @author Joakim Bjornstad, Visma Consulting
 */
@Component
@Endpoint
public class DokumentbehandlingEndpoint implements DokumentbehandlingPortType {

	private static final Log log = new Log(DokumentbehandlingEndpoint.class);
	private static final String EXCEPTION_MESSAGE = "SOAPkall feilet";
	private static final String NAMESPACE_URI = "http://dokumentbehandling.brevogarkiv.tjenester.nav.no/";


	private final DokumentbehandlingProvider dokumentbehandlingProvider;

	@Autowired
	public DokumentbehandlingEndpoint(DokumentbehandlingProvider dokumentbehandlingProvider) {
		this.dokumentbehandlingProvider = dokumentbehandlingProvider;
	}

	@Override
	public HentDokumentResponse2 hentDokument(HentDokumentRequest hentDokumentRequest) {
		try {
			//return dokumentbehandlingProvider.hentDokument(hentDokumentRequest);
		} catch (RuntimeException e) {
			if (e.getCause() != null && e.getCause() instanceof BrevSecurityException) {
				throw e;
			}
			log.error("hentDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}
		return null;
	}

	@Override
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "lagreDokument")
	@ResponsePayload
	public void lagreDokument(@RequestPayload LagreDokumentRequest lagreDokumentRequest) {
		try {
			dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest);
		} catch (RuntimeException e) {
			log.error("lagreDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}  catch (BrevException e) {
			log.error("ferdigstillDokument", EXCEPTION_MESSAGE, e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void avbrytDokument(AvbrytDokumentRequest avbrytDokumentRequest) {
		try {
			dokumentbehandlingProvider.avbrytDokument(avbrytDokumentRequest);
		} catch (RuntimeException e) {
			log.error("avbrytDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}  catch (BrevException e) {
			log.error("ferdigstillDokument", EXCEPTION_MESSAGE, e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "ferdigstillDokument")
	@ResponsePayload
	public void ferdigstillDokument(@RequestPayload FerdigstillDokumentRequest ferdigstillDokumentRequest) {
		try {
			dokumentbehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);
		} catch (RuntimeException e) {
			log.error("ferdigstillDokument", EXCEPTION_MESSAGE, e);
			throw e;
		} catch (BrevException e) {
			log.error("ferdigstillDokument", EXCEPTION_MESSAGE, e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "ping")
	@ResponsePayload
	public void ping(@RequestPayload PingRequest pingRequest) {
		try {
			dokumentbehandlingProvider.ping(pingRequest);
		} catch (RuntimeException e) {
			log.error("ping", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}
}
