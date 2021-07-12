package no.nav.brevserver.provider.map;

import no.nav.brevserver.service.dokumentbehandling.to.LagreDokumentRequest;

/**
 * Interface for mapping between webservice and domain lagreDokument request
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface LagreDokumentRequestMapper {
	/**
	 * Maps from webservice request to domain request for lagreDokument
	 *
	 * @param lagreDokumentRequest The webservice request
	 * @return The mapped domain request
	 */
	LagreDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest lagreDokumentRequest);
}
