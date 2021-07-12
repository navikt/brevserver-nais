package no.nav.brevserver.service.dokumentbehandling;

import no.nav.brevserver.service.dokumentbehandling.to.AvbrytDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentResponse;
import no.nav.brevserver.service.dokumentbehandling.to.LagreDokumentRequest;

/**
 * Service that does dokumentbehandling.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface DokumentbehandlingService {
	/**
	 * Hent dokument with information in the request
	 *
	 * @param hentDokumentRequest The request object
	 * @return The response object containing the dokumentdata
	 */
	HentDokumentResponse hentDokument(HentDokumentRequest hentDokumentRequest);

	/**
	 * Lagre dokument in request
	 *
	 * @param lagreDokumentRequest
	 */
	void lagreDokument(LagreDokumentRequest lagreDokumentRequest);

	/**
	 * Avbryt dokument in request
	 *
	 * @param avbrytDokumentRequest
	 */
	void avbrytDokument(AvbrytDokumentRequest avbrytDokumentRequest);

	/**
	 * Ferdigstill dokument in request
	 *
	 * @param ferdigstillDokumentRequest
	 */
	void ferdigstillDokument(FerdigstillDokumentRequest ferdigstillDokumentRequest);

	/**
	 * Pings the service to check if it is alive
	 */
	void ping();
}
