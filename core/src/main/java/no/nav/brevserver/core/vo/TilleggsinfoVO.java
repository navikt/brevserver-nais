package no.nav.brevserver.core.vo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Representation of Tilleggsinfo in a Journalpost
 * */

public class TilleggsinfoVO {
    private String type;
    private String verdi;

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("type", type).append("verdi", verdi)
                .toString();
    }

    public String getType() {
        return type;
    }

    public String getVerdi() {
        return verdi;
    }

    public void setType(String string) {
        type = string;
    }

    public void setVerdi(String string) {
        verdi = string;
    }

}
