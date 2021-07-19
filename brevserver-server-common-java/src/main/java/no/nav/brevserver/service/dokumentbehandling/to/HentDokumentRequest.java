package no.nav.brevserver.service.dokumentbehandling.to;

import org.apache.commons.lang3.Validate;

/**
 * Domain request object for hent dokument operations on the service layer
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class HentDokumentRequest extends AbstractDocumentRequest {

	public void validate() {
		Validate.notNull(brevStatus, "brevStatus must be set");
		Validate.notNull(brevStatus.getSystemID(), "brevStatus.systemID must be set");
		Validate.notNull(brevStatus.getBrevreferanse(), "brevStatus.brevreferanse must be set");
		Validate.notNull(brevStatus.getToken(), "brevStatus.token must be set");
	}
}
