package no.nav.brevserver.service.dokumentbehandling;

import no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest;

/**
 * Service that does ferdigstill dokument
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface FerdigstillDokumentService {
	/**
	 * Ferdigstill dokument detailed in the request
	 *
	 * @param request The request object
	 */
	void ferdigstillDokument(FerdigstillDokumentRequest request);
}
