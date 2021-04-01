package no.nav.brevserver.server.common.vo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents a Journalpost
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 * @author Rune Røren, Accenture
 *
 */
public class JournalpostVO {
    private String dato;
    private String dokType;
    private String status;
    private String innhBeskr;
    private String innhBeskrUoff;
    private String uDatertDok;
    private String tilgangsgruppe;
    private String tilgangskode;
    private String hjemmelUoff;

    private List<AvsenderMottakerVO> avsenderMottaker = new ArrayList<AvsenderMottakerVO>();
    private List<TilleggsinfoVO> tilleggsinfo = new ArrayList<TilleggsinfoVO>();
    private List<DokBeskrivelseVO> dokbeskrivelse = new ArrayList<DokBeskrivelseVO>();
    
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("dokType", dokType)
            .append("dato", dato)
            .append("status", status)
            .append("innholdsbeskrivelse", innhBeskr)
            .append("innholdsbeskrivelseUoff", innhBeskrUoff)
            .append("udatertDokument", uDatertDok)
            .append("tilgangsgruppe", tilgangsgruppe)
            .append("tilgangskode", tilgangskode)
            .append("hjemmelUoff", hjemmelUoff)
            .append("avsenderMottaker", avsenderMottaker)
            .append("tilleggsinfo", tilleggsinfo)
            .append("dokbeskrivelse", dokbeskrivelse)
            .toString();
    }

    public AvsenderMottakerVO getAvsenderMottaker(int i) {
        return (AvsenderMottakerVO) avsenderMottaker.get(i);
    }

    public void addAvsenderMottaker(AvsenderMottakerVO am) {
        avsenderMottaker.add(am);
    }

    public int getAvsenderMottakerSize() {
        return avsenderMottaker.size();
    }

    public TilleggsinfoVO getTilleggsinfo(int i) {
        return (TilleggsinfoVO) tilleggsinfo.get(i);
    }

    public void addTilleggsinfo(TilleggsinfoVO ti) {
        tilleggsinfo.add(ti);
    }

    public int getTilleggsinfoSize() {
        return tilleggsinfo.size();
    }

    public DokBeskrivelseVO getDokBeskrivelse(int i) {
        return (DokBeskrivelseVO) dokbeskrivelse.get(i);
    }

    public void addDokBeskrivelse(DokBeskrivelseVO db) {
        dokbeskrivelse.add(db);
    }

    public int getDokBeskrivelseSize() {
        return dokbeskrivelse.size();
    }

    public String getDato() {
        return dato;
    }

    public String getDokType() {
        return dokType;
    }
    
    public String getHjemmelUoff() {
        return hjemmelUoff;
    }

    public String getInnhBeskr() {
        return innhBeskr;
    }

    public String getInnhBeskrUoff() {
        return innhBeskrUoff;
    }

    public String getStatus() {
        return status;
    }

    public String getTilgangsgruppe() {
        return tilgangsgruppe;
    }

    public String getTilgangskode() {
        return tilgangskode;
    }
    
    public String getUDatertDok() {
        return uDatertDok;
    }

    public void setDato(String string) {
        dato = string;
    }

    public void setDokType(String string) {
        dokType = string;
    }

    public void setHjemmelUoff(String string) {
        hjemmelUoff = string;
    }
    
    public void setInnhBeskr(String string) {
        innhBeskr = string;
    }

    public void setInnhBeskrUoff(String string) {
        innhBeskrUoff = string;
    }

    public void setStatus(String string) {
        status = string;
    }

    public void setTilgangsgruppe(String string) {
        tilgangsgruppe = string;
    }

    public void setTilgangskode(String string) {
        tilgangskode = string;
    }

    public void setUDatertDok(String string) {
        uDatertDok = string;
    }

}
