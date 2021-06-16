package no.nav.brevserver.nais.support;

import no.nav.brevserver.server.common.to.AvbrytDokumentRequest;
import org.springframework.stereotype.Component;

@Component
public interface AvbrytDokumentRequestMapper {
	/**
	 * Maps from webservice request to domain request for avbrytDokument
	 *
	 * @param avbrytDokumentRequest The webservice request
	 * @return The mapped domain request
	 */
	AvbrytDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest avbrytDokumentRequest);
}
