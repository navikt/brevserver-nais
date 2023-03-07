package no.nav.brevserver.app.dokumentbehandling.to;

import org.apache.commons.lang3.Validate;

/**
 * Domain request object for ferdigstill dokument operations on the service layer
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
