package no.nav.brevserver.core.vo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents an Avsender and Mottaker
 */
public class AvsenderMottakerVO {
    private String innholdstype;
    private String kopi;
    private String behAns;
    private String fornavn;
    private String etternavn;
    private String avsMottUoff;
    private String saksbehAdmEnhet;
    private String saksbehIdent;
    private String forsendMaate;

    private String forkortSaksbehEnhet;
    private String saksbehInit;
    private String journalEnhet;
    private String avskrivMaate;
    private String avskrivDato;

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("innholdstype", innholdstype)
            .append("kopi", kopi)
            .append("behAns", behAns)
            .append("fornavn", fornavn)
            .append("etternavn", etternavn)
            .append("avsMottUoff", avsMottUoff)
            .append("saksbehAdmEnhet", saksbehAdmEnhet)
            .append("saksbehIdent", saksbehIdent)
            .append("forsendMaate", forsendMaate)
            .append("forkortSaksbehEnhet", forkortSaksbehEnhet)
            .append("saksbehInit", saksbehInit)
            .append("journalEnhet", journalEnhet)
            .append("avskrivMaate", avskrivMaate)
            .append("avskrivDato", avskrivDato)
            .toString();
    }

    public String getAvskrivDato() {
        return avskrivDato;
    }

    public String getAvskrivMaate() {
        return avskrivMaate;
    }

    public String getAvsMottUoff() {
        return avsMottUoff;
    }

    public String getBehAns() {
        return behAns;
    }

    public String getEtternavn() {
        return etternavn;
    }

    public String getForkortSaksbehEnhet() {
        return forkortSaksbehEnhet;
    }

    public String getFornavn() {
        return fornavn;
    }

    public String getForsendMaate() {
        return forsendMaate;
    }

    public String getInnholdstype() {
        return innholdstype;
    }

    public String getJournalEnhet() {
        return journalEnhet;
    }

    public String getKopi() {
        return kopi;
    }

    public String getSaksbehAdmEnhet() {
        return saksbehAdmEnhet;
    }

    public String getSaksbehIdent() {
        return saksbehIdent;
    }

    public String getSaksbehInit() {
        return saksbehInit;
    }

    public void setAvskrivDato(String string) {
        avskrivDato = string;
    }

    public void setAvskrivMaate(String string) {
        avskrivMaate = string;
    }

    public void setAvsMottUoff(String string) {
        avsMottUoff = string;
    }

    public void setBehAns(String string) {
        behAns = string;
    }

    public void setEtternavn(String string) {
        etternavn = string;
    }

    public void setForkortSaksbehEnhet(String string) {
        forkortSaksbehEnhet = string;
    }

    public void setFornavn(String string) {
        fornavn = string;
    }

    public void setForsendMaate(String string) {
        forsendMaate = string;
    }

    public void setInnholdstype(String string) {
        innholdstype = string;
    }

    public void setJournalEnhet(String string) {
        journalEnhet = string;
    }

    public void setKopi(String string) {
        kopi = string;
    }

    public void setSaksbehAdmEnhet(String string) {
        saksbehAdmEnhet = string;
    }

    public void setSaksbehIdent(String string) {
        saksbehIdent = string;
    }

    public void setSaksbehInit(String string) {
        saksbehInit = string;
    }

}
