package no.nav.brevserver.ws.dokumentbehandling.support;

import no.nav.brevserver.ws.dokumentbehandling.to.AvbrytDokumentRequest;


public interface AvbrytDokumentRequestMapper {
	/**
	 * Maps from webservice request to domain request for avbrytDokument
	 *
	 * @param avbrytDokumentRequest The webservice request
	 * @return The mapped domain request
	 */
	AvbrytDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest avbrytDokumentRequest);
}
