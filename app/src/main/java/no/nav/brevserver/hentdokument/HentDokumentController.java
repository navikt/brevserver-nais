package no.nav.brevserver.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.valueOf;

@Slf4j
@Protected
@Validated
@RestController
@RequestMapping("rest")
public class HentDokumentController {

	public static final String OEBS_SYSTEMID = "FS10";
	private static final String HENTDOKUMENT_FUNKSJONELL_FEILMELDING = "hentdokument feilet funksjonelt med feilmelding: {}";

	private final HentDokumentService hentDokumentService;

	public HentDokumentController(HentDokumentService hentDokumentService) {
		this.hentDokumentService = hentDokumentService;
	}

	@GetMapping(value = "/hentdokument/{dokId}")
	public ResponseEntity<?> hentDokument(
			@PathVariable("dokId")
			@NotBlank(message = "brevreferanse kan ikke være blank")
			@Pattern(regexp = "^\\d{1,32}$", message = "brevreferanse må være numerisk og må ha 32 eller færre siffer.")
			String brevreferanse
	) {
		log.info("hentdokument henter dokument med brevreferanse={}", brevreferanse);

		Bilag bilag = hentDokumentService.hentDokumentFraBrevlager(brevreferanse);

		if (bilag == null) {
			log.info("hentdokument fant ikke dokument med brevreferanse={} i databasen", brevreferanse);
			return ResponseEntity.notFound().build();
		}

		var contentType = bilag.contentType();
		if (!APPLICATION_PDF_VALUE.equals(contentType)) {
			log.info("dokument med brevreferanse={} har contentType={}, som ikke kan vises frem i nettleseren", brevreferanse, contentType);

			return ResponseEntity.notFound().build();
		}

		log.info("hentdokument hentet dokument med brevreferanse={}", brevreferanse);

		return ResponseEntity.ok()
				.contentType(valueOf(contentType))
				.header(CONTENT_DISPOSITION, format("inline; filename=%s_%s%s", OEBS_SYSTEMID, brevreferanse, mapExtension(contentType)))
				.body(bilag.brevdata());
	}

	private String mapExtension(String contentType) {
		return switch (contentType) {
			case "application/msword.docx" -> ".docx";
			case "text/rtf" -> ".rtf";
			default -> ".pdf";
		};
	}

	@ExceptionHandler({
			ConstraintViolationException.class
	})
	public ResponseEntity<Object> inputValidationExceptionHandler(Exception e) {
		log.warn(HENTDOKUMENT_FUNKSJONELL_FEILMELDING, e.getMessage(), e);

		return getResponseEntity(BAD_REQUEST, e.getMessage());
	}

	private static ResponseEntity<Object> getResponseEntity(HttpStatus status, String message) {
		return ResponseEntity.status(status)
				.contentType(APPLICATION_JSON)
				.body(format("\"%s\"", message));
	}

}