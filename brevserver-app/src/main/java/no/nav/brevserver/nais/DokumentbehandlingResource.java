package no.nav.brevserver.nais;

import io.swagger.annotations.Api;
import no.nav.brevserver.nais.swagger.SwaggerLagreBrev;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevSecurityException;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.to.HentDokumentRequest;
import no.nav.brevserver.server.common.to.HentDokumentResponse;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(description = "Tjenester for å arkivere i brevserver")
@RequestMapping("rest")
public class DokumentbehandlingResource {

	private Logger logger = LoggerFactory.getLogger(this.getClass());


	private static final String EXCEPTION_MESSAGE = "SOAPkall feilet";

	private DokumentbehandlingProvider dokumentbehandlingProvider;

	@Autowired
	public DokumentbehandlingResource(DokumentbehandlingProvider dokumentbehandlingProvider) {
		this.dokumentbehandlingProvider = dokumentbehandlingProvider;
	}


	@GetMapping("/hent")
	public @ResponseBody HentDokumentResponse hentDokument(HentDokumentRequest hentDokumentRequest) {
		//hentDokumentRequest.validate();
		try {
			//FIXME: TEST Data
			HentDokumentRequest request = new HentDokumentRequest();
			BrevStatusVO v0 = new BrevStatusVO();
			v0.setStatus("FERDIG");
			v0.setSystemID("FS10");
			v0.setToken("1093");
			v0.setBrevreferanse("1096");
			request.setBrevStatus(v0);
			return dokumentbehandlingProvider.hentDokument(request);
		} catch (RuntimeException e) {
			if (e.getCause() != null && e.getCause() instanceof BrevSecurityException) {
				throw e;
			}
			logger.error("hentDokument", EXCEPTION_MESSAGE, e);
			throw e;
			//TODO: Exceptionhandling
		} catch (BrevTechnicalException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} catch (BrevFunctionalException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	@SwaggerLagreBrev
	@PostMapping("/lagre")
	public void lagreDokument(LagreDokumentRequest lagreDokumentRequest) throws BrevException {
		try {
			dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest);
		} catch (RuntimeException e) {
			logger.error("lagreDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}
/*
	@PostMapping("/avbryt")
	public void avbrytDokument(AvbrytDokumentRequest avbrytDokumentRequest) {
		try {
			dokumentbehandlingProvider.avbrytDokument(avbrytDokumentRequest);
		} catch (RuntimeException e) {
			logger.error("avbrytDokument", EXCEPTION_MESSAGE, e);
			throw e;
		}
	}

	@PostMapping("/ferdigstill")
	public void ferdigstillDokument(FerdigstillDokumentRequest ferdigstillDokumentRequest) {
		try {
			dokumentbehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);
		} catch (RuntimeException e) {
			logger.error("ferdigstillDokument", EXCEPTION_MESSAGE, e);
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
