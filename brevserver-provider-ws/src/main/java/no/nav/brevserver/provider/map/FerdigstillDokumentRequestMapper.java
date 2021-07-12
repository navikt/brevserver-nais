package no.nav.brevserver.provider.map;

import no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest;

/**
 * Interface for mapping between webservice and domain ferdigstillDokument requests
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface FerdigstillDokumentRequestMapper {
	/**
	 * Maps from webservice request to domain request for ferdigstillDokument
	 *
	 * @param ferdigstillDokumentRequest The webservice request
	 * @return The mapped domain request
	 */
	FerdigstillDokumentRequest map(
			no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest ferdigstillDokumentRequest);
}
