package no.nav.brevserver.server.common.vo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Representation of Dokumentbeskrivelse in a Journalpost
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 * @author Rune Røren, Accenture
 */
public class DokBeskrivelseVO {
    private String kategori;
    private String status;

    private String versjon;
    private String variant;
    private String lagringsFormat;
    private String lagringsEnhet;

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("kategori", kategori)
            .append("status", status)
            .append("versjon", versjon)
            .append("variant", variant)
            .append("lagringsFormat", lagringsFormat)
            .append("lagringsEnhet", lagringsEnhet).toString();
    }

    public String getKategori() {
        return kategori;
    }

    public String getLagringsEnhet() {
        return lagringsEnhet;
    }

    public String getLagringsFormat() {
        return lagringsFormat;
    }

    public String getStatus() {
        return status;
    }

    public String getVariant() {
        return variant;
    }

    public String getVersjon() {
        return versjon;
    }

    public void setKategori(String string) {
        kategori = string;
    }

    public void setLagringsEnhet(String string) {
        lagringsEnhet = string;
    }

    public void setLagringsFormat(String string) {
        lagringsFormat = string;
    }

    public void setStatus(String string) {
        status = string;
    }

    public void setVariant(String string) {
        variant = string;
    }

    public void setVersjon(String string) {
        versjon = string;
    }

}
