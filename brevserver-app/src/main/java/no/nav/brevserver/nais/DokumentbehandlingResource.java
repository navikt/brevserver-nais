package no.nav.brevserver.nais;

import no.nav.brevserver.server.common.exception.BrevSecurityException;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentResponse;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("rest")
public class DokumentbehandlingResource {

	private Logger logger = LoggerFactory.getLogger(this.getClass());


	private static final String EXCEPTION_MESSAGE = "SOAPkall feilet";

	private DokumentbehandlingProvider dokumentbehandlingProvider;

	public DokumentbehandlingResource() {
		dokumentbehandlingProvider = new DokumentbehandlingProvider();
	}


	@GetMapping("/hent")
	public @ResponseBody HentDokumentResponse hentDokument() {
		try {
			return dokumentbehandlingProvider.hentDokument();
		} catch (RuntimeException e) {
			if (e.getCause() != null && e.getCause() instanceof BrevSecurityException) {
				throw e;
			}
			logger.error("hentDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}

	/*
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


	 */

	@GetMapping("/ping")
	public void ping(PingRequest pingRequest) {
		try {
			dokumentbehandlingProvider.ping(pingRequest);
		} catch (RuntimeException e) {
			logger.error("ping", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}


}
