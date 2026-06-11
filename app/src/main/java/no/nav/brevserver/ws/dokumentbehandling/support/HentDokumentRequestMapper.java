package no.nav.brevserver.ws.dokumentbehandling.support;

import no.nav.brevserver.ws.dokumentbehandling.to.HentDokumentRequest;

/**
 * Interface for mapping between webservice and domain hentDokument requests
 */
public interface HentDokumentRequestMapper {
	/**
	 * Maps from webservice request to domain request for hentDokument
	 *
	 * @param hentDokumentRequest The webservice request
	 * @return The mapped domain request
	 */
	HentDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest hentDokumentRequest);
}
