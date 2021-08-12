package no.nav.brevserver.app.dokumentbehandling.to;

import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import org.apache.commons.lang3.Validate;

/**
 * Common domain request object properties and operations on the service layer
 *
 * @author Marius Thøring, Visma Consulting
 */
public class AbstractDocumentRequest {

	protected BrevStatusVO brevStatus;
	protected BrevVO brev;
	protected BrevVO pdfBrev;
	protected boolean newDocument;

	protected void validateBrevStatus() {
		Validate.notNull(brevStatus.getSystemID(), "brevStatus.systemID must be set");
		Validate.notNull(brevStatus.getBrevreferanse(), "brevStatus.brevreferanse must be set");
		Validate.notNull(brevStatus.getToken(), "brevStatus.token must be set");
		if (isNewDocument()) {
			Validate.notNull(brevStatus.getReturKoe(), "brevStatus.returKoe must be set if newDocument is true");
			Validate.notNull(brevStatus.getBrevmal(), "brevStatus.brevmal must be set if newDocument is true");
		}
	}

	protected void validateBrev() {
		Validate.notNull(brev.getSystemID(), "brev.systemID must be set");
		Validate.notNull(brev.getBrevreferanse(), "brev.brevreferanse must be set");
		validateEditableContentType(brev.getContentType());
		Validate.notNull(brev.getBrevdata(), "brev.brevdata must be set");
		Validate.notNull(brev.getBrukerID(), "brev.brukerID must be set");
	}

	protected void validatePdfBrev() {
		Validate.notNull(pdfBrev.getSystemID(), "pdfBrev.systemID must be set");
		Validate.notNull(pdfBrev.getBrevreferanse(), "pdfBrev.brevreferanse must be set");
		validateNonEditableContentType(pdfBrev.getContentType());
		Validate.notNull(pdfBrev.getBrevdata(), "pdfBrev.brevdata must be set");
		Validate.notNull(pdfBrev.getBrukerID(), "pdfBrev.brukerID must be set");
	}

	private void validateEditableContentType(String contentType) {
		Validate.isTrue(FilType.XML.getContentType().equals(contentType) ||
						FilType.RTF.getContentType().equals(contentType) ||
						FilType.DOCX.getContentType().equals(contentType),
				"brev.contentType must be either " + FilType.XML.getContentType() +
						", " + FilType.RTF.getContentType() +
						" or " + FilType.DOCX.getContentType() + " but was: " + contentType);
	}

	private void validateNonEditableContentType(String contentType) {
		Validate.isTrue(FilType.PDF.getContentType().equals(contentType),
				"pdfBrev.contentType must be " + FilType.PDF.getContentType());
	}

	public BrevStatusVO getBrevStatus() {
		return brevStatus;
	}

	public void setBrevStatus(BrevStatusVO brevStatus) {
		this.brevStatus = brevStatus;
	}

	public BrevVO getBrev() {
		return brev;
	}

	public void setBrev(BrevVO brev) {
		this.brev = brev;
	}

	public BrevVO getPdfBrev() {
		return pdfBrev;
	}

	public void setPdfBrev(BrevVO pdfBrev) {
		this.pdfBrev = pdfBrev;
	}

	public boolean isNewDocument() {
		return newDocument;
	}

	public void setNewDocument(boolean newDocument) {
		this.newDocument = newDocument;
	}
}
