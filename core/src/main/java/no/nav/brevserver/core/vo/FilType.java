package no.nav.brevserver.core.vo;

/**
 * Enum that contains the JOARK fileTypeCode and internet media type for a given filetype used
 */
public enum FilType {

	XML("XML", "application/xml"),
	PDF("PDF", "application/pdf"),
	PDFA("PDFA", "application/pdf"),
	RTF("RTF", "text/rtf"),
	DOCX("DOCX", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

	private final String joarkCode;
	private final String contentType;

	FilType(String joarkCode, String contentType) {
		this.joarkCode = joarkCode;
		this.contentType = contentType;
	}

	/**
	 * Returns the FileTypeCode in JOARK for the given filetype
	 *
	 * @return The JOARK FileTypeCode
	 */
	public String getJoarkCode() {
		return joarkCode;
	}

	/**
	 * Returns the internet media type (Content-Type) for the given filetype
	 *
	 * @return The Content-Type
	 */
	public String getContentType() {
		return contentType;
	}
}
