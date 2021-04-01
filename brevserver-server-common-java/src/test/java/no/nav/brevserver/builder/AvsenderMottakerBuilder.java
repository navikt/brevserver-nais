package no.nav.brevserver.builder;

import no.nav.brevserver.server.common.vo.AvsenderMottakerVO;

public class AvsenderMottakerBuilder implements Builder<AvsenderMottakerVO> {

    private AvsenderMottakerBuilder() {

    }

    public static AvsenderMottakerBuilder getAvsenderMottakerBuilder() {
        return new AvsenderMottakerBuilder();
    }

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

    public AvsenderMottakerBuilder innholdstype(String value) {
        this.innholdstype = value;
        return this;
    }

    public AvsenderMottakerBuilder kopi(String value) {
        this.kopi = value;
        return this;
    }

    public AvsenderMottakerBuilder behAns(String value) {
        this.behAns = value;
        return this;
    }

    public AvsenderMottakerBuilder fornavn(String value) {
        this.fornavn = value;
        return this;
    }

    public AvsenderMottakerBuilder etternavn(String value) {
        this.etternavn = value;
        return this;
    }

    public AvsenderMottakerBuilder avsMottUoff(String value) {
        this.avsMottUoff = value;
        return this;
    }

    public AvsenderMottakerBuilder saksbehAdmEnhet(String value) {
        this.saksbehAdmEnhet = value;
        return this;
    }

    public AvsenderMottakerBuilder saksbehIdent(String value) {
        this.saksbehIdent = value;
        return this;
    }

    public AvsenderMottakerBuilder forsendMaate(String value) {
        this.forsendMaate = value;
        return this;
    }

    public AvsenderMottakerBuilder forkortSaksbehEnhet(String value) {
        this.forkortSaksbehEnhet = value;
        return this;
    }

    public AvsenderMottakerBuilder saksbehInit(String value) {
        this.saksbehInit = value;
        return this;
    }

    public AvsenderMottakerBuilder journalEnhet(String value) {
        this.journalEnhet = value;
        return this;
    }

    public AvsenderMottakerBuilder avskrivMaate(String value) {
        this.avskrivMaate = value;
        return this;
    }

    public AvsenderMottakerBuilder avskrivDato(String value) {
        this.avskrivDato = value;
        return this;
    }

    @Override
    public AvsenderMottakerVO build() {
        AvsenderMottakerVO avsenderMottaker = new AvsenderMottakerVO();
        avsenderMottaker.setInnholdstype(innholdstype);
        avsenderMottaker.setKopi(kopi);
        avsenderMottaker.setBehAns(behAns);
        avsenderMottaker.setFornavn(fornavn);
        avsenderMottaker.setEtternavn(etternavn);
        avsenderMottaker.setAvsMottUoff(avsMottUoff);
        avsenderMottaker.setSaksbehAdmEnhet(saksbehAdmEnhet);
        avsenderMottaker.setSaksbehIdent(saksbehIdent);
        avsenderMottaker.setForsendMaate(forsendMaate);
        avsenderMottaker.setForkortSaksbehEnhet(forkortSaksbehEnhet);
        avsenderMottaker.setSaksbehInit(saksbehInit);
        avsenderMottaker.setJournalEnhet(journalEnhet);
        avsenderMottaker.setAvskrivMaate(avskrivMaate);
        avsenderMottaker.setAvskrivDato(avskrivDato);
        return avsenderMottaker;
    }
}
