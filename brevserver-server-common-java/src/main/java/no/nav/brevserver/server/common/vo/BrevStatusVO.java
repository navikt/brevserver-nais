package no.nav.brevserver.server.common.vo;

import no.nav.brevserver.server.common.config.KnappStatus;
import no.nav.brevserver.server.common.config.Konstanter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class BrevStatusVO implements Serializable {
	private static final long serialVersionUID = 7514533773610729921L;

	private String brevreferanse;
	private String systemID;
	private String returKoe;
	private String bestillerBrukerID;
	private String brevmal;
	private String status;
	private String modus;
	private String token;
	private String format;
	private String skrivertype;
	private String skriver;
	private String arkiver;
	private String skuff;
	private String passord;
	private KnappStatus knappStatus = null;

	public String getBrevreferanse() {
		return brevreferanse;
	}

	public void setBrevreferanse(String brevreferanse) {
		this.brevreferanse = brevreferanse != null ? brevreferanse.trim() : null;
	}

	public String getSystemID() {
		return systemID;
	}

	public void setSystemID(String systemID) {
		this.systemID = systemID;
	}

	public String getReturKoe() {
		return returKoe;
	}

	public void setReturKoe(String returKoe) {
		this.returKoe = returKoe;
	}

	public String getBestillerBrukerID() {
		return bestillerBrukerID;
	}

	public void setBestillerBrukerID(String bestillerBrukerID) {
		this.bestillerBrukerID = bestillerBrukerID;
	}

	public String getBrevmal() {
		return brevmal;
	}

	public void setBrevmal(String brevmal) {
		this.brevmal = brevmal;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Modus er i utgangspunktet blank, Dersom modus="frabrevlager" forutsetter vi at dokumentet skal hentes fra brevlageret.
	 * Bestilling om generering av nytt brev til Dialogue vil ikke gjøres.
	 *
	 * @return Returns a String
	 */
	public String getModus() {
		return modus;
	}

	/**
	 * Modus er i utgangspunktet blank, Dersom modus="frabrevlager" forutsetter vi at dokumentet skal hentes fra brevlageret.
	 * Bestilling om generering av nytt brev til Dialogue vil ikke gjøres.
	 *
	 * @param modus The modus to set
	 */
	public void setModus(String modus) {
		this.modus = modus;
	}

	public String getToken() {
		return token;
	}

	/**
	 * Returnerer en sensurert versjon av token som kan brukes i logging.
	 * Formatet som blir returnert tilsvarer {@value Konstanter#MASKED_PASSWORD} + opptil de 3 siste tegnene i tokenet.
	 *
	 * @return The censored token
	 */
	public String getCensoredToken() {
		if (token != null) {
			return Konstanter.MASKED_PASSWORD + StringUtils.right(token, 3);
		} else {
			return Konstanter.MASKED_PASSWORD;
		}
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getSkrivertype() {
		return skrivertype;
	}

	public void setSkrivertype(String skrivertype) {
		this.skrivertype = skrivertype;
	}

	public String getSkriver() {
		return skriver;
	}

	public void setSkriver(String skriver) {
		this.skriver = skriver;
	}

	public String getArkiver() {
		return arkiver;
	}

	public void setArkiver(String arkiver) {
		this.arkiver = arkiver;
	}

	public String getSkuff() {
		return skuff;
	}

	public void setSkuff(String skuff) {
		this.skuff = skuff;
	}

	public String getPassord() {
		return passord;
	}

	public void setPassord(String passord) {
		this.passord = passord;
	}

	public KnappStatus getKnappStatus() {
		if (knappStatus == null) {
			knappStatus = new KnappStatus(KnappStatus.getDefaultValue());
		}

		return knappStatus;
	}

	public void setKnappStatus(KnappStatus knappStatus) {
		this.knappStatus = knappStatus;
	}
}
