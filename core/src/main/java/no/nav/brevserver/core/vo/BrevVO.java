package no.nav.brevserver.core.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BrevVO implements Serializable {
	private static final long serialVersionUID = 6699141929697725601L;

	private String brevreferanse;
	private String systemID;
	private String lagerStatus;
	private Timestamp endret;
	private byte[] brevdata;
	private String brukerID;
	private String contentType;


	public String getBrevreferanse() {
		return brevreferanse;
	}

	public void setBrevreferanse(String brevreferanse) {
		this.brevreferanse = brevreferanse;
	}

	public String getSystemID() {
		return systemID;
	}

	public void setSystemID(String systemID) {
		this.systemID = systemID;
	}

	public String getLagerStatus() {
		return lagerStatus;
	}

	public void setLagerStatus(String status) {
		this.lagerStatus = status;
	}

	public Timestamp getEndret() {
		return endret;
	}

	public void setEndret(Timestamp endret) {
		this.endret = endret;
	}

	public byte[] getBrevdata() {
		return brevdata;
	}

	public void setBrevdata(byte[] brevdata) {
		this.brevdata = brevdata;
	}

	public String getBrukerID() {
		return brukerID;
	}

	public void setBrukerID(String brukerID) {
		this.brukerID = brukerID;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public BrevVO copy() {
		BrevVO brevVOCopy = new BrevVO();
		brevVOCopy.setBrevreferanse(brevreferanse);
		brevVOCopy.setSystemID(systemID);
		brevVOCopy.setLagerStatus(lagerStatus);
		brevVOCopy.setEndret(endret);
		brevVOCopy.setBrevdata(brevdata);
		brevVOCopy.setBrukerID(brukerID);
		brevVOCopy.setContentType(contentType);
		return brevVOCopy;
	}
}
