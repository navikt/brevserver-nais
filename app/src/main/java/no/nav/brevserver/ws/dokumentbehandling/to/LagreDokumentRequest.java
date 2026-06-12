package no.nav.brevserver.ws.dokumentbehandling.to;

import org.apache.commons.lang3.Validate;

/**
 * Domain request object for lagre dokument operations on the service layer
 */
public class LagreDokumentRequest extends AbstractDocumentRequest {

	public void validate() {
		Validate.notNull(brevStatus, "brevStatus must be set");
		Validate.notNull(brev, "brev must be set");
		validateBrevStatus();
		validateBrev();
	}
}
