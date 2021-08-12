package no.nav.brevserver.nais.support;

import no.nav.brevserver.app.dokumentbehandling.to.HentDokumentRequest;

/**
 * Interface for mapping between webservice and domain hentDokument requests
 *
 * @author Joakim Bjørnstad, Visma Consulting
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
