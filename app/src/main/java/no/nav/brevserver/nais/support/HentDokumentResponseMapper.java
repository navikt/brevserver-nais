package no.nav.brevserver.nais.support;

import no.nav.brevserver.app.dokumentbehandling.to.HentDokumentResponse;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;

/**
 * Interface for mapping between webservice and domain hentDokument response
 */
public interface HentDokumentResponseMapper {
	/**
	 * Maps from webservice domain to webservice response for hentDokument
	 *
	 * @param hentDokumentResponse The domain response
	 * @return The mapped webservice request
	 */
	HentDokumentResponse2 map(HentDokumentResponse hentDokumentResponse);
}
