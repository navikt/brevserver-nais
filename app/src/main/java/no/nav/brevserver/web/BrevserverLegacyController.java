package no.nav.brevserver.web;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.security.token.support.core.api.Unprotected;
import org.apache.log4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static no.nav.brevserver.core.constants.MDCConstants.BREVREFERANSE_KEY;
import static no.nav.brevserver.core.constants.MDCConstants.SYSTEMID_KEY;
import static no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId.BREVREFERANSE_LENGTH;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.valueOf;

/**
 * Tilbyr hentdokument endepunkt for å vise dokument i nettleser, i stedet for gjennom brevklient
 *
 * @deprecated hentdokument tjenesten fases ut til fordel for bilag appen
 */
@Controller
@Unprotected
@Slf4j
@Deprecated(since = "1.15.0")
public class BrevserverLegacyController {

	static final String FAGSYSTEM_OEBS = "FS10";
	private final BrevlagerService brevlagerService;

	public BrevserverLegacyController(BrevlagerService brevlagerService) {
		this.brevlagerService = brevlagerService;
	}

	@GetMapping(value = "/hentdokument")
	public ResponseEntity<?> hentDokument(@RequestParam("systemid") String systemid,
										  @RequestParam("dokid") String dokid,
										  @RequestParam("token") String token) {
		try {
			log.info("legacy hentdokument - henter dokument med systemId={}, brevreferanse={}", systemid, dokid);
			validerInput(systemid, dokid);
			MDC.put(SYSTEMID_KEY, systemid);
			MDC.put(BREVREFERANSE_KEY, dokid);
			BrevVO brevVO = brevlagerService.hentDokumentFromBrevlagerOrJoark(mapBrevStatusVO(systemid, dokid, token));
			if (brevVO == null) {
				return ResponseEntity.status(NOT_FOUND)
						.contentType(TEXT_HTML)
						.body("""
								<!DOCTYPE html>
								<html lang="no">
								<head>
									<meta charset="UTF-8">
								    <title>Hentdokument fant ikke dokumentet</title>
								</head>
								<body>
								    <h1>Hentdokument fant ikke dokumentet med systemid=%s, dokid=%s</h1>
								    <p>Det kan være at dokumentet fremdeles er under oppretting eller det kan ha feilet under produksjon.</p>
								    <p>Prøv igjen om litt eller ta kontakt med Team Dokumentløsninger gjennom brukerstøtte eller på NAV IT sin slack.</p>
								</body>
								</html>
								""".formatted(systemid, dokid));
			}
			log.info("legacy hentdokument hentet dokument med systemId={}, brevreferanse={}", systemid, dokid);
			String contentType = brevVO.getContentType();
			return ResponseEntity.ok()
					.contentType(valueOf(contentType))
					.header(CONTENT_DISPOSITION, "inline; filename=" + systemid + "_" + dokid + mapExtension(contentType))
					.body(brevVO.getBrevdata());
		} catch (BrevTechnicalException e) {
			log.error("legacy hentdokument feilet teknisk. message=" + e.getMessage(), e);
			return ResponseEntity.internalServerError().body(e.getMessage());
		} catch (BrevFunctionalException | IllegalArgumentException e) {
			log.warn("legacy hentdokument feilet funksjonelt. message=" + e.getMessage(), e);
			return ResponseEntity.badRequest().body(e.getMessage());
		} finally {
			MDC.clear();
		}
	}

	private void validerInput(String systemId, String brevreferanse) {
		if (!FAGSYSTEM_OEBS.equals(systemId)) {
			throw new IllegalArgumentException("systemId må være FS10 som er OEBS. systemId=" + systemId);
		}
		if (!isNumeric(brevreferanse) || brevreferanse.length() > BREVREFERANSE_LENGTH) {
			throw new IllegalArgumentException("brevreferanse må være numerisk og må ha " + BREVREFERANSE_LENGTH + " eller færre siffer. brevreferanse=" + brevreferanse);
		}
	}

	private BrevStatusVO mapBrevStatusVO(String systemId, String brevreferanse, String token) {
		BrevStatusVO brevStatus = new BrevStatusVO();
		brevStatus.setSystemID(systemId);
		brevStatus.setBrevreferanse(brevreferanse);
		brevStatus.setToken(token);
		return brevStatus;
	}

	private String mapExtension(String contentType) {
		return switch (contentType) {
			case "application/msword.docx" -> ".docx";
			case "text/rtf" -> ".rtf";
			default -> ".pdf";
		};
	}
}