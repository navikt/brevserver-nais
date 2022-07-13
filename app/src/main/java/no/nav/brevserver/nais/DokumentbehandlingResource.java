package no.nav.brevserver.nais;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevFinnesAlleredeException;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.exception.BrevSecurityException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.nais.swagger.SwaggerLagreBrev;
import no.nav.security.token.support.core.api.Protected;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;

@Api(description = "Tjenester for å arkivere i brevserver")
@RequestMapping("rest")
@RestController
@Slf4j
@Protected
public class DokumentbehandlingResource {

	private static final String EXCEPTION_MESSAGE = "Rest-Kall feilet";
	public static final String APPLICATION_XML = "application/xml";

	private DokumentbehandlingProvider dokumentbehandlingProvider;

	@Autowired
	public DokumentbehandlingResource(DokumentbehandlingProvider dokumentbehandlingProvider) {
		this.dokumentbehandlingProvider = dokumentbehandlingProvider;
	}


	@GetMapping("/hent")
	public @ResponseBody HentDokumentResponse2 hentDokument(HentDokumentRequest hentDokumentRequest) {

		log.info("Prøver å hente dokument: " + hentDokumentRequest.getBrevreferanse() + " fra " + hentDokumentRequest.getSystemId());
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
	public void ferdigstillDokument(@RequestPart FerdigstillDokumentRequest ferdigstillDokumentRequest, @RequestPart MultipartFile pdfDokument, @RequestPart String pdfMimetype, @RequestPart MultipartFile redDokument, @RequestPart String redMimetype, @RequestPart(required = false) Boolean tillatRekjoring) throws BrevException {
		try {
			ferdigstillDokumentRequest.setRedDokument(createDataHandlerForFile(redDokument.getInputStream(), redMimetype));
			ferdigstillDokumentRequest.setPdfDokument(createDataHandlerForFile(pdfDokument.getInputStream(), pdfMimetype));
			dokumentbehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);
		} catch (BrevFinnesAlleredeException e) {
			if (tillatRekjoring != null && tillatRekjoring) {
				log.info("Brev med referanse er allerede opprettet, tillater rekjoring");
			} else {
				log.error("ferdigstillDokument", EXCEPTION_MESSAGE, e);
				throw e;
			}
		} catch (RuntimeException e) {
			log.error("ferdigstillDokument", EXCEPTION_MESSAGE, e);
			throw e;
		} catch (IOException e) {
			log.error("ferdigstillDokument", EXCEPTION_MESSAGE, e);
			throw new RuntimeException(e.getMessage());
		}
	}

	private DataHandler createDataHandlerForFile(InputStream stream, String contentType) throws IOException {
		return new DataHandler(new ByteArrayDataSource(IOUtils.toByteArray(stream), contentType));
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
