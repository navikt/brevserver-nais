package no.nav.brevserver.ws.dokumentbehandling;

import no.nav.brevserver.nais.DokumentbehandlingProvider;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevSecurityException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.log.Log;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokument;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.DokumentbehandlingPortType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokument;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokument;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokument;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.ObjectFactory;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.Ping;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

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
	private ObjectFactory objectFactory ;

	@Autowired
	public DokumentbehandlingEndpoint(DokumentbehandlingProvider dokumentbehandlingProvider) {
		this.dokumentbehandlingProvider = dokumentbehandlingProvider;
		this.objectFactory = new ObjectFactory();
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "hentDokument")
	@ResponsePayload
	public JAXBElement<HentDokumentResponse2> hentDokumentEndpoint(@RequestPayload HentDokument hentDokument) {
		return createJaxbElement(hentDokument(hentDokument.getRequest()));
	}

	@Override
	public HentDokumentResponse2 hentDokument(HentDokumentRequest hentDokumentRequest) {
		try {
			HentDokumentResponse2 dok = dokumentbehandlingProvider.hentDokument(hentDokumentRequest);
			HentDokumentResponse2 hentDokumentResponse2 = this.objectFactory.createHentDokumentResponse2();
			hentDokumentResponse2.setDokumentData(dok.getDokumentData());
			hentDokumentResponse2.setKnappStatus(dok.getKnappStatus());
			return hentDokumentResponse2;
		} catch (RuntimeException e) {
			if (e.getCause() != null && e.getCause() instanceof BrevSecurityException) {
				throw e;
			}
			log.error("hentDokument", EXCEPTION_MESSAGE, e);
			throw e;
		} catch (BrevTechnicalException e) {
			log.error("hentDokument", EXCEPTION_MESSAGE, e);
			throw new RuntimeException(e.getMessage());
		} catch (BrevFunctionalException e) {
			log.error("hentDokument", EXCEPTION_MESSAGE, e);
			throw new RuntimeException(e.getMessage());
		}
	}


	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "lagreDokument")
	@ResponsePayload
	public void lagreDokumentEndpoint(@RequestPayload LagreDokument lagreDokument) {
		lagreDokument(lagreDokument.getRequest());
	}

	@Override
	public void lagreDokument(LagreDokumentRequest lagreDokumentRequest) {
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

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "avbrytDokument")
	@ResponsePayload
	public void avbrytDokumentEndpoint(@RequestPayload AvbrytDokument avbrytDokument) {
		avbrytDokument(avbrytDokument.getRequest());
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

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "ferdigstillDokument")
	@ResponsePayload
	public void ferdigstillDokumentEndpoint(@RequestPayload FerdigstillDokument ferdigstillDokument) {
		ferdigstillDokument(ferdigstillDokument.getRequest());
	}

	@Override
	public void ferdigstillDokument(FerdigstillDokumentRequest ferdigstillDokumentRequest) {
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

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "ping")
	@ResponsePayload
	public void pingEndpoint(@RequestPayload Ping ping) {
		ping(ping.getRequest());
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

	private JAXBElement<HentDokumentResponse2> createJaxbElement(HentDokumentResponse2 object) {
		Class clazz = HentDokumentResponse2.class;
		return new JAXBElement<>(new QName(clazz.getSimpleName()), clazz, object);
	}
}
