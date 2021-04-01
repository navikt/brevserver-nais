package no.nav.brevserver.service.dokumentbehandling;

import no.nav.brevserver.service.dokumentbehandling.to.AvbrytDokumentRequest;

/**
 * Service that does avbryt dokument
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface AvbrytDokumentService {
	/**
	 * Avbryt the dokument detailed in the request
	 *
	 * @param request The request object
	 */
	void avbrytDokument(AvbrytDokumentRequest request);
}
