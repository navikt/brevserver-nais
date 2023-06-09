package no.nav.brevserver.nais;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevFinnesAlleredeException;
import no.nav.brevserver.core.mdc.MDCConstants;
import no.nav.security.token.support.core.api.Protected;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static no.nav.brevserver.core.constants.MDCConstants.BREVREFERANSE_KEY;
import static no.nav.brevserver.core.constants.MDCConstants.NAV_CALL_ID;
import static no.nav.brevserver.core.constants.MDCConstants.SYSTEMID_KEY;
import static no.nav.brevserver.core.constants.MDCConstants.X_CORRELATION_ID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RequestMapping("rest")
@RestController
@Slf4j
@Protected
public class DokumentbehandlingResource {

	private static final String EXCEPTION_MESSAGE = "Rest-Kall feilet";

	private final DokumentbehandlingProvider dokumentbehandlingProvider;

	public DokumentbehandlingResource(DokumentbehandlingProvider dokumentbehandlingProvider) {
		this.dokumentbehandlingProvider = dokumentbehandlingProvider;
	}

	@PostMapping("/lagre")
	public void lagreDokument(LagreDokumentRequest lagreDokumentRequest) throws BrevException {
		try {
			handleMDCCallId();
			MDC.put(SYSTEMID_KEY, lagreDokumentRequest.getSystemId());
			MDC.put(BREVREFERANSE_KEY, lagreDokumentRequest.getBrevreferanse());

			log.info("Brevserver har mottat kall for å lagre dokument " + lagreDokumentRequest.getBrevreferanse() + " fra: " + lagreDokumentRequest.getSystemId());
			dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest);
		} catch (RuntimeException e) {
			log.error("lagreDokument", EXCEPTION_MESSAGE, e);
			throw e;
		} finally {
			MDC.clear();
		}
	}

	@PostMapping("/avbryt")
	public void avbrytDokument(AvbrytDokumentRequest avbrytDokumentRequest) throws BrevException {
		try {
			handleMDCCallId();
			MDC.put(SYSTEMID_KEY, avbrytDokumentRequest.getSystemId());
			MDC.put(BREVREFERANSE_KEY, avbrytDokumentRequest.getBrevreferanse());

			log.info("Brevserver har mottatt kall for å avbryte dokument " + avbrytDokumentRequest.getBrevreferanse() + " fra " + avbrytDokumentRequest.getSystemId());
			dokumentbehandlingProvider.avbrytDokument(avbrytDokumentRequest);
		} catch (RuntimeException e) {
			log.error("avbrytDokument", EXCEPTION_MESSAGE, e);
			throw e;
		} finally {
			MDC.clear();
		}
	}

	@PostMapping("/ferdigstill")
	public void ferdigstillDokument(@RequestPart FerdigstillDokumentRequest ferdigstillDokumentRequest, @RequestPart MultipartFile pdfDokument, @RequestPart String pdfMimetype, @RequestPart MultipartFile redDokument, @RequestPart String redMimetype, @RequestPart(required = false) Boolean tillatRekjoring) throws BrevException {
		try {
			handleMDCCallId();
			MDC.put(SYSTEMID_KEY, ferdigstillDokumentRequest.getSystemId());
			MDC.put(BREVREFERANSE_KEY, ferdigstillDokumentRequest.getBrevreferanse());

			log.info("Brevserver har mottatt kall for å ferdigstille dokument: " + ferdigstillDokumentRequest.getBrevreferanse() + " fra: " + ferdigstillDokumentRequest.getSystemId());
			ferdigstillDokumentRequest.setRedDokument(createDataHandlerForFile(redDokument.getInputStream(), redMimetype));
			ferdigstillDokumentRequest.setPdfDokument(createDataHandlerForFile(pdfDokument.getInputStream(), pdfMimetype));
			dokumentbehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);
		} catch (BrevFinnesAlleredeException e) {
			if (tillatRekjoring != null && tillatRekjoring) {
				log.info("Brev med referanse er allerede opprettet, tillater rekjoring");
			} else {
				log.warn("ferdigstillDokument", EXCEPTION_MESSAGE, e);
				throw e;
			}
		} catch (RuntimeException e) {
			log.error("ferdigstillDokument", EXCEPTION_MESSAGE, e);
			throw e;
		} catch (IOException e) {
			log.error("ferdigstillDokument", EXCEPTION_MESSAGE, e);
			throw new RuntimeException(e.getMessage());
		} finally {
			MDC.clear();
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

	private void handleMDCCallId() {
		try {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			final String navCallId = request.getHeader(NAV_CALL_ID);
			if (isNotBlank(navCallId)) {
				MDC.put(MDCConstants.MDC_CALL_ID, navCallId);
				return;
			}

			final String xCorrelationId = request.getHeader(X_CORRELATION_ID);
			if (isNotBlank(xCorrelationId)) {
				MDC.put(MDCConstants.MDC_CALL_ID, xCorrelationId);
				return;
			}

			final String callIdHeader = request.getHeader("callId");
			if (isNotBlank(callIdHeader)) {
				MDC.put(MDCConstants.MDC_CALL_ID, callIdHeader);
				return;
			}
		} catch (Exception e) {
			//noop
		}
		// Fallback
		MDC.put(MDCConstants.MDC_CALL_ID, UUID.randomUUID().toString());
	}
}
