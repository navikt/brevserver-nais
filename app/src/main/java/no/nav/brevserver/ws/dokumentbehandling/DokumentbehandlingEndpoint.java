package no.nav.brevserver.ws.dokumentbehandling;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevFinnesIkkeException;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.exception.BrevserverFunctionalException;
import no.nav.brevserver.core.exception.BrevserverTechnicalException;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokument;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.DokumentbehandlingPortType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokument;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokument;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokument;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.ObjectFactory;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.Ping;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.slf4j.MDC;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import static no.nav.brevserver.core.constants.MDCConstants.MDC_CALL_ID;

/// Implementation of the JAX-WS generated service interface DokumentbehandlingPortType.
/// Delegates to DokumentbehandlingProvider at the provider layer.
@Slf4j
@Endpoint
public class DokumentbehandlingEndpoint implements DokumentbehandlingPortType {

	private static final String NAMESPACE_URI = "http://dokumentbehandling.brevogarkiv.tjenester.nav.no/";
	private final ObjectFactory objectFactory;

	private final DokumentbehandlingProvider dokumentbehandlingProvider;

	public DokumentbehandlingEndpoint(DokumentbehandlingProvider dokumentbehandlingProvider) {
		this.dokumentbehandlingProvider = dokumentbehandlingProvider;
		this.objectFactory = new ObjectFactory();
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "hentDokument")
	@ResponsePayload
	public HentDokumentResponse hentDokumentEndpoint(@RequestPayload HentDokument hentDokument) {
		try {
			MDC.put(MDC_CALL_ID, hentDokument.getRequest().getBrevreferanse() + hentDokument.getRequest().getSystemId());
			HentDokumentResponse response = this.objectFactory.createHentDokumentResponse();
			response.setResponse(hentDokument(hentDokument.getRequest()));
			return response;
		} finally {
			MDC.clear();
		}
	}

	@Override
	public HentDokumentResponse2 hentDokument(HentDokumentRequest hentDokumentRequest) {
		try {
			return dokumentbehandlingProvider.hentDokument(hentDokumentRequest);
		} catch (BrevFinnesIkkeException e) {
			log.info("hentDokument finner ikke brev med brevreferanse={}, systemId={}",
					hentDokumentRequest.getBrevreferanse(), hentDokumentRequest.getSystemId(), e);
			throw new RuntimeException(e.getMessage());
		} catch (BrevserverFunctionalException | BrevFunctionalException e) {
			log.warn("hentDokument funksjonell feil for brevreferanse={}, systemId={}",
					hentDokumentRequest.getBrevreferanse(), hentDokumentRequest.getSystemId(), e);
			throw new RuntimeException(e.getMessage());
		} catch (BrevserverTechnicalException | BrevTechnicalException e) {
			log.error("hentDokument teknisk feil for brevreferanse={}, systemId={}",
					hentDokumentRequest.getBrevreferanse(), hentDokumentRequest.getSystemId(), e);
			throw new RuntimeException(e.getMessage());
		} catch (RuntimeException e) {
			log.error("hentDokument teknisk feil for brevreferanse={}, systemId={}",
					hentDokumentRequest.getBrevreferanse(), hentDokumentRequest.getSystemId(), e);
			throw e;
		}
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "lagreDokument")
	@ResponsePayload
	public void lagreDokumentEndpoint(@RequestPayload LagreDokument lagreDokument) {
		try {
			MDC.put(MDC_CALL_ID, lagreDokument.getRequest().getBrevreferanse() + lagreDokument.getRequest().getSystemId());
			lagreDokument(lagreDokument.getRequest());
		} finally {
			MDC.clear();
		}
	}

	@Override
	public void lagreDokument(LagreDokumentRequest lagreDokumentRequest) {
		try {
			dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest);
		} catch (RuntimeException e) {
			log.error("lagreDokument feilet for brevreferanse={}, systemId={}",
					lagreDokumentRequest.getBrevreferanse(), lagreDokumentRequest.getSystemId(), e);
			throw e;
		} catch (BrevException e) {
			log.error("lagreDokument feilet for brevreferanse={}, systemId={}",
					lagreDokumentRequest.getBrevreferanse(), lagreDokumentRequest.getSystemId(), e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "avbrytDokument")
	@ResponsePayload
	public void avbrytDokumentEndpoint(@RequestPayload AvbrytDokument avbrytDokument) {
		try {
			MDC.put(MDC_CALL_ID, avbrytDokument.getRequest().getBrevreferanse() + avbrytDokument.getRequest().getSystemId());
			avbrytDokument(avbrytDokument.getRequest());
		} finally {
			MDC.clear();
		}
	}

	@Override
	public void avbrytDokument(AvbrytDokumentRequest avbrytDokumentRequest) {
		try {
			dokumentbehandlingProvider.avbrytDokument(avbrytDokumentRequest);
		} catch (RuntimeException e) {
			log.error("avbrytDokument feilet for brevreferanse={}, systemId={}",
					avbrytDokumentRequest.getBrevreferanse(), avbrytDokumentRequest.getSystemId(), e);
			throw e;
		} catch (BrevException e) {
			log.error("avbrytDokument feilet for brevreferanse={}, systemId={}",
					avbrytDokumentRequest.getBrevreferanse(), avbrytDokumentRequest.getSystemId(), e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "ferdigstillDokument")
	@ResponsePayload
	public void ferdigstillDokumentEndpoint(@RequestPayload FerdigstillDokument ferdigstillDokument) {
		try {
			MDC.put(MDC_CALL_ID, ferdigstillDokument.getRequest().getBrevreferanse() + ferdigstillDokument.getRequest().getSystemId());
			ferdigstillDokument(ferdigstillDokument.getRequest());
		} finally {
			MDC.clear();
		}
	}

	@Override
	public void ferdigstillDokument(FerdigstillDokumentRequest ferdigstillDokumentRequest) {
		try {
			dokumentbehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);
		} catch (RuntimeException e) {
			log.error("ferdigstillDokument feilet for brevreferanse={}, systemId={}",
					ferdigstillDokumentRequest.getBrevreferanse(), ferdigstillDokumentRequest.getSystemId(), e);
			throw e;
		} catch (BrevException e) {
			log.error("ferdigstillDokument feilet for brevreferanse={}, systemId={}",
					ferdigstillDokumentRequest.getBrevreferanse(), ferdigstillDokumentRequest.getSystemId(), e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "ping")
	@ResponsePayload
	public void pingEndpoint(@RequestPayload Ping ping) {
		ping(ping.getRequest());
	}

	@Override
	public void ping(PingRequest pingRequest) {
		// noop
	}
}
