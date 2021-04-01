package no.nav.brevserver.builder;

import no.nav.brevserver.server.common.vo.BrevVO;

public class BrevBuilder implements Builder<BrevVO> {

    private BrevBuilder() {

    }

    public static BrevBuilder getBrevBuilder() {
        return new BrevBuilder();
    }

    private String brevreferanse;
    private String systemID;
    private String lagerStatus;
    private byte[] brevdata;
    private String brukerID;
    private String contentType;

    public BrevBuilder brevreferanse(String value) {
        this.brevreferanse = value;
        return this;
    }

    public BrevBuilder systemID(String value) {
        this.systemID = value;
        return this;
    }

    public BrevBuilder lagerStatus(String value) {
        this.lagerStatus = value;
        return this;
    }

    public BrevBuilder brevdata(byte[] value) {
        this.brevdata = value;
        return this;
    }

    public BrevBuilder brukerID(String value) {
        this.brukerID = value;
        return this;
    }

    public BrevBuilder contentType(String value) {
        this.contentType = value;
        return this;
    }

    @Override
    public BrevVO build() {
        BrevVO brev = new BrevVO();
        brev.setBrevreferanse(brevreferanse);
        brev.setSystemID(systemID);
        brev.setLagerStatus(lagerStatus);
        brev.setBrevdata(brevdata);
        brev.setBrukerID(brukerID);
        brev.setContentType(contentType);
        return brev;
    }
}
