package no.nav.brevserver.builder;

import no.nav.brevserver.server.common.config.KnappStatus;
import no.nav.brevserver.server.common.vo.BrevStatusVO;

public class BrevStatusBuilder implements Builder<BrevStatusVO> {

    private BrevStatusBuilder() {

    }

    public static BrevStatusBuilder getBrevStatusBuilder() {
        return new BrevStatusBuilder();
    }

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
    private KnappStatus knappStatus;

    public BrevStatusBuilder brevreferanse(String value) {
        this.brevreferanse = value;
        return this;
    }

    public BrevStatusBuilder systemID(String value) {
        this.systemID = value;
        return this;
    }

    public BrevStatusBuilder returKoe(String value) {
        this.returKoe = value;
        return this;
    }

    public BrevStatusBuilder bestillerBrukerID(String value) {
        this.bestillerBrukerID = value;
        return this;
    }

    public BrevStatusBuilder brevmal(String value) {
        this.brevmal = value;
        return this;
    }

    public BrevStatusBuilder status(String value) {
        this.status = value;
        return this;
    }

    public BrevStatusBuilder modus(String value) {
        this.modus = value;
        return this;
    }

    public BrevStatusBuilder token(String value) {
        this.token = value;
        return this;
    }

    public BrevStatusBuilder format(String value) {
        this.format = value;
        return this;
    }

    public BrevStatusBuilder skrivertype(String value) {
        this.skrivertype = value;
        return this;
    }

    public BrevStatusBuilder skriver(String value) {
        this.skriver = value;
        return this;
    }

    public BrevStatusBuilder arkiver(String value) {
        this.arkiver = value;
        return this;
    }

    public BrevStatusBuilder skuff(String value) {
        this.skuff = value;
        return this;
    }

    public BrevStatusBuilder passord(String value) {
        this.passord = value;
        return this;
    }

    public BrevStatusBuilder knappStatus(KnappStatus value) {
        this.knappStatus = value;
        return this;
    }

    @Override
    public BrevStatusVO build() {
        BrevStatusVO brevStatus = new BrevStatusVO();
        brevStatus.setBrevreferanse(brevreferanse);
        brevStatus.setSystemID(systemID);
        brevStatus.setReturKoe(returKoe);
        brevStatus.setBestillerBrukerID(bestillerBrukerID);
        brevStatus.setBrevmal(brevmal);
        brevStatus.setStatus(status);
        brevStatus.setModus(modus);
        brevStatus.setToken(token);
        brevStatus.setFormat(format);
        brevStatus.setSkrivertype(skrivertype);
        brevStatus.setSkriver(skriver);
        brevStatus.setArkiver(arkiver);
        brevStatus.setSkuff(skuff);
        brevStatus.setPassord(passord);
        brevStatus.setKnappStatus(knappStatus);
        return brevStatus;
    }
}
