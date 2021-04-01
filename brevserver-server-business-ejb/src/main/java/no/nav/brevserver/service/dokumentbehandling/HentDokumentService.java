package no.nav.brevserver.service.dokumentbehandling;

import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentResponse;

/**
 * Service that does hent dokument
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface HentDokumentService {
	/**
	 * Does a hent dokument operation based on information the request
	 *
	 * @param request The request object
	 * @return The response object, containing the brevdata
	 */
	HentDokumentResponse hentDokument(HentDokumentRequest request);
}
