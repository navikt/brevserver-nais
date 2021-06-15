package no.nav.brevserver.server.common.to;

import org.apache.commons.lang.Validate;

/**
 * Domain request object for avbryt dokument operations on the service layer
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class AvbrytDokumentRequest extends AbstractDocumentRequest {

	public void validate() {
		Validate.notNull(brevStatus, "brevStatus must be set");
		Validate.notNull(brevStatus.getSystemID(), "brevStatus.systemID must be set");
		Validate.notNull(brevStatus.getBrevreferanse(), "brevStatus.brevreferanse must be set");
		Validate.notNull(brevStatus.getToken(), "brevStatus.token must be set");
	}
}
