package no.nav.brevserver.server.common.to;

import org.apache.commons.lang.Validate;

/**
 * Domain request object for lagre dokument operations on the service layer
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class LagreDokumentRequest extends AbstractDocumentRequest {

	public void validate() {
		Validate.notNull(brevStatus, "brevStatus must be set");
		Validate.notNull(brev, "brev must be set");
		validateBrevStatus();
		validateBrev();
	}
}
