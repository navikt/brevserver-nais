package no.nav.brevserver.provider.map;


import no.nav.brevserver.server.common.to.HentDokumentRequest;

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
