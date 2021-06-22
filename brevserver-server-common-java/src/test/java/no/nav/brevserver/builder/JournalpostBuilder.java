package no.nav.brevserver.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.nav.brevserver.server.common.vo.AvsenderMottakerVO;
import no.nav.brevserver.server.common.vo.DokBeskrivelseVO;
import no.nav.brevserver.server.common.vo.JournalpostVO;
import no.nav.brevserver.server.common.vo.TilleggsinfoVO;

/**
 * Builder of Journalposts
 * 
 * @author Joakim Bjornstad, Visma Consulting
 * 
 */
public class JournalpostBuilder implements Builder<JournalpostVO> {

    private JournalpostBuilder() {

    }

    public static JournalpostBuilder getJournalpostBuilder() {
        return new JournalpostBuilder();
    }

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

    public JournalpostBuilder dato(String value) {
        this.dato = value;
        return this;
    }

    public JournalpostBuilder dokType(String value) {
        this.dokType = value;
        return this;
    }

    public JournalpostBuilder status(String value) {
        this.status = value;
        return this;
    }

    public JournalpostBuilder innhBeskr(String value) {
        this.innhBeskr = value;
        return this;
    }

    public JournalpostBuilder innhBeskrUoff(String value) {
        this.innhBeskrUoff = value;
        return this;
    }

    public JournalpostBuilder uDatertDok(String value) {
        this.uDatertDok = value;
        return this;
    }

    public JournalpostBuilder tilgangsgruppe(String value) {
        this.tilgangsgruppe = value;
        return this;
    }

    public JournalpostBuilder tilgangskode(String value) {
        this.tilgangskode = value;
        return this;
    }

    public JournalpostBuilder hjemmelUoff(String value) {
        this.hjemmelUoff = value;
        return this;
    }

    public JournalpostBuilder avsenderMottaker(AvsenderMottakerVO... values) {
        this.avsenderMottaker.addAll(Arrays.asList(values));
        return this;
    }

    public JournalpostBuilder tilleggsinfo(TilleggsinfoVO... values) {
        this.tilleggsinfo.addAll(Arrays.asList(values));
        return this;
    }

    public JournalpostBuilder dokbeskrivelse(DokBeskrivelseVO... values) {
        this.dokbeskrivelse.addAll(Arrays.asList(values));
        return this;
    }

    @Override
    public JournalpostVO build() {
        JournalpostVO journalpost = new JournalpostVO();
        journalpost.setDato(dato);
        journalpost.setDokType(dokType);
        journalpost.setStatus(status);
        journalpost.setInnhBeskr(innhBeskr);
        journalpost.setInnhBeskrUoff(innhBeskrUoff);
        journalpost.setUDatertDok(uDatertDok);
        journalpost.setTilgangsgruppe(tilgangsgruppe);
        journalpost.setTilgangskode(tilgangskode);
        journalpost.setHjemmelUoff(hjemmelUoff);

        for (AvsenderMottakerVO am : avsenderMottaker) {
            journalpost.addAvsenderMottaker(am);
        }

        for (TilleggsinfoVO ti : tilleggsinfo) {
            journalpost.addTilleggsinfo(ti);
        }

        for (DokBeskrivelseVO db : dokbeskrivelse) {
            journalpost.addDokBeskrivelse(db);
        }

        return journalpost;
    }
}
