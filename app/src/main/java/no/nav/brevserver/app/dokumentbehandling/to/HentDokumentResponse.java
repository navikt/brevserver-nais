package no.nav.brevserver.app.dokumentbehandling.to;

/**
 * Domain response object for hent dokument operations on the service layer
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class HentDokumentResponse {
	private byte[] dokumentData;
	private String contentType;
	private String knappStatus;

	public HentDokumentResponse() {
	}

	public byte[] getDokumentData() {
		return dokumentData;
	}

	public void setDokumentData(byte[] dokumentData) {
		this.dokumentData = dokumentData;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getKnappStatus() {
		return knappStatus;
	}

	public void setKnappStatus(String knappStatus) {
		this.knappStatus = knappStatus;
	}
}
