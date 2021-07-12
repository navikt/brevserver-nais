package no.nav.brevserver.service.dokumentbehandling;

import no.nav.brevserver.service.dokumentbehandling.to.LagreDokumentRequest;

/**
 * Service that does lagre dokument
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface LagreDokumentService {
	/**
	 * Lagre the dokument detailed in the request
	 *
	 * @param request The request object
	 */
	void lagreDokument(LagreDokumentRequest request);
}
