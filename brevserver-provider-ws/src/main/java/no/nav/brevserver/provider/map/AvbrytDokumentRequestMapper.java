package no.nav.brevserver.provider.map;

import no.nav.brevserver.service.dokumentbehandling.to.AvbrytDokumentRequest;

/**
 * Interface for mapping between webservice and domain avbrytDokument requests
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface AvbrytDokumentRequestMapper {
	/**
	 * Maps from webservice request to domain request for avbrytDokument
	 *
	 * @param avbrytDokumentRequest The webservice request
	 * @return The mapped domain request
	 */
	AvbrytDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest avbrytDokumentRequest);
}
