package no.nav.brevserver.hentdokument;

import com.nimbusds.jwt.SignedJWT;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
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

	public static final Pattern OEBS_PATTERN = Pattern.compile("^\\d{1,32}$");
	public static final Pattern BISYS_PATTERN = Pattern.compile("^BIF\\d{1,29}$");

	private static final String HENTDOKUMENT_FUNKSJONELL_FEILMELDING = "hentdokument feilet funksjonelt med feilmelding: {}";

	private final HentDokumentService hentDokumentService;

	public HentDokumentController(HentDokumentService hentDokumentService) {
		this.hentDokumentService = hentDokumentService;
	}

	@GetMapping(value = {
		// denne eksisterer midlertidig for bakoverkompatibilitet med bilag
		"/hentdokument/{dokId}",
		"/hentdokument/{dokId}/{system}" })
	public ResponseEntity<?> hentDokument(
			@RequestHeader(AUTHORIZATION) String authorization,
			@PathVariable("dokId")
			@NotBlank(message = "brevreferanse kan ikke være blank")
			String brevreferanse,
			@PathVariable(value = "system", required = false) String system
	) {
		var hentDokumentSystem = HentDokumentSystem.parse(system);
		log.info("hentdokument henter dokument med brevreferanse={} og systemId={}", brevreferanse, hentDokumentSystem.getSystemId());
		oboTokenAuthorizedForSystem(authorization, hentDokumentSystem);
		validateBrevReferanseForSystem(hentDokumentSystem, brevreferanse);

		Bilag bilag = hentDokumentService.hentDokumentFraBrevlager(brevreferanse, hentDokumentSystem.getSystemId());

		if (bilag == null) {
			log.info("hentdokument fant ikke dokument med brevreferanse={} og systemId={} i databasen", brevreferanse, hentDokumentSystem.getSystemId());
			return ResponseEntity.notFound().build();
		}

		var contentType = bilag.contentType();
		if (!APPLICATION_PDF_VALUE.equals(contentType)) {
			log.info("dokument med brevreferanse={} har contentType={}, som ikke kan vises frem i nettleseren", brevreferanse, contentType);

			return ResponseEntity.notFound().build();
		}

		log.info("hentdokument hentet dokument med brevreferanse={} og systemId={}", brevreferanse, hentDokumentSystem.getSystemId());

		return ResponseEntity.ok()
				.contentType(valueOf(contentType))
				.header(CONTENT_DISPOSITION, format("inline; filename=%s_%s%s", hentDokumentSystem.getSystemId(), brevreferanse, mapExtension(contentType)))
				.body(bilag.brevdata());
	}

	private void validateBrevReferanseForSystem(HentDokumentSystem scope, @NotBlank(message = "brevreferanse kan ikke være blank") String brevreferanse) {
		switch (scope) {
			case OEBS -> {
				if (!OEBS_PATTERN.matcher(brevreferanse).matches())
					throw new ConstraintViolationException("brevreferanse må være numerisk og må ha 32 eller færre siffer.", emptySet());
			}
			case BISYS -> {
				if (!BISYS_PATTERN.matcher(brevreferanse).matches())
					throw new ConstraintViolationException("brevreferanse må starte med \"BIF\" etterfulgt av tall, og inneholde maksimalt 32 tegn.", emptySet());
			}
		}
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

	private static void oboTokenAuthorizedForSystem(String authorizationHeader, HentDokumentSystem requestedSystem) {
		try {
			String token = authorizationHeader.split(" ")[1];
			SignedJWT decodedJWT = SignedJWT.parse(token);

			String scopes = decodedJWT.getJWTClaimsSet().getStringClaim("scp");
			if (scopes != null && Arrays.asList(scopes.split("\\s+")).contains(requestedSystem.getScopeName())) {
				return;
			}
			log.warn("hentdokument avvist fordi tokenet ikke inneholder hverken oebs eller bisys-scope. Scopes={}", scopes);
			throw new KunneIkkeParseTillattScopeException("hentdokument avvist fordi tokenet ikke inneholder påkrevd scope.");
		} catch (IndexOutOfBoundsException|ParseException e) {
			log.warn("hentdokument kunne ikke finne scope i token", e);
			throw new KunneIkkeParseTillattScopeException();
		}
	}

	enum HentDokumentSystem {
		OEBS("FS10"), BISYS("BI12");

		private final String systemId;

		HentDokumentSystem(String systemId) {
			this.systemId = systemId;
		}

		public String getSystemId() {
			return systemId;
		}

		public String getScopeName() {
			return name().toLowerCase();
		}

		public static HentDokumentSystem parse(String system) {
			return switch (system) {
				case "bisys" -> BISYS;
				case "oebs" -> OEBS;
				// denne er midlertidig frem til bilag er oppdatert
				case null -> OEBS;
				default -> throw new RuntimeException("Unknown system!");
			};
		}
	}
}