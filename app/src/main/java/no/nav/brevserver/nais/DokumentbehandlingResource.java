package no.nav.brevserver.nais;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.nais.swagger.SwaggerLagreBrev;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.exception.BrevSecurityException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(description = "Tjenester for å arkivere i brevserver")
@RequestMapping("rest")
@RestController
@Slf4j
public class DokumentbehandlingResource {

	private static final String EXCEPTION_MESSAGE = "SOAPkall feilet";

	private DokumentbehandlingProvider dokumentbehandlingProvider;

	@Autowired
	public DokumentbehandlingResource(DokumentbehandlingProvider dokumentbehandlingProvider) {
		this.dokumentbehandlingProvider = dokumentbehandlingProvider;
	}


	@GetMapping("/hent")
	public @ResponseBody HentDokumentResponse2 hentDokument(HentDokumentRequest hentDokumentRequest) {

		log.info("Prøver å hente dokument: " + hentDokumentRequest.getBrevreferanse() + " fra " + hentDokumentRequest.getSystemId() + " med token: " + hentDokumentRequest.getToken());
		try {
			return dokumentbehandlingProvider.hentDokument(hentDokumentRequest);
		} catch (RuntimeException e) {
			if (e.getCause() != null && e.getCause() instanceof BrevSecurityException) {
				throw e;
			}
			log.error("hentDokument", EXCEPTION_MESSAGE, e);
			throw e;
		} catch (BrevTechnicalException e) {
			log.warn("hentDokument", e);
			throw new RuntimeException(e.getMessage());
		} catch (BrevFunctionalException e) {
			log.warn("hentDokument", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@SwaggerLagreBrev
	@PostMapping("/lagre")
	public void lagreDokument(LagreDokumentRequest lagreDokumentRequest) throws BrevException {
		try {
			dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest);
		} catch (RuntimeException e) {
			log.error("lagreDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}

	@PostMapping("/avbryt")
	public void avbrytDokument(AvbrytDokumentRequest avbrytDokumentRequest) throws BrevException {
		try {
			dokumentbehandlingProvider.avbrytDokument(avbrytDokumentRequest);
		} catch (RuntimeException e) {
			log.error("avbrytDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}

	@PostMapping("/ferdigstill")
	public void ferdigstillDokument(FerdigstillDokumentRequest ferdigstillDokumentRequest) throws BrevException {
		try {
			dokumentbehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);
		} catch (RuntimeException e) {
			log.error("ferdigstillDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}

	@GetMapping("/ping")
	public void ping(PingRequest pingRequest) {
		try {
			dokumentbehandlingProvider.ping(pingRequest);
		} catch (RuntimeException e) {
			log.error("ping", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}
}
