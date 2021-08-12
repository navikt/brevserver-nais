package no.nav.brevserver.nais.support;

import no.nav.brevserver.app.dokumentbehandling.to.AvbrytDokumentRequest;


public interface AvbrytDokumentRequestMapper {
	/**
	 * Maps from webservice request to domain request for avbrytDokument
	 *
	 * @param avbrytDokumentRequest The webservice request
	 * @return The mapped domain request
	 */
	AvbrytDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest avbrytDokumentRequest);
}
