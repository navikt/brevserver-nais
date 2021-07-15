package no.nav.brevserver.service.dokumentbehandling.to;

import org.apache.commons.lang.Validate;

/**
 * Domain request object for ferdigstill dokument operations on the service layer
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class FerdigstillDokumentRequest extends AbstractDocumentRequest {

	public void validate() {
		Validate.notNull(brevStatus, "brevStatus must be set");
		Validate.notNull(brev, "brev must be set");
		Validate.notNull(pdfBrev, "pdfBrev must be set");
		validateBrevStatus();
		validateBrev();
		validatePdfBrev();
	}
}
