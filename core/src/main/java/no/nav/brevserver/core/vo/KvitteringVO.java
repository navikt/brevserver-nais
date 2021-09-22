package no.nav.brevserver.core.vo;

/**
 * KvitteringVO blir brukt ifm batch. Den inneholder brev og meta-informasjon om
 * brev ( status )
 * */
public class KvitteringVO extends BrevVO {
    private static final long serialVersionUID = 9096941770385857411L;
    private String feilkode;
    private String feilniva;
    // Dette er et workaround for å kunne hente ut malpakke av kvitteringen.
    private String tmpMalpakke; 
    private JournalpostVO journalpost;

    /**
     * Gets the feilkode
     * 
     * @return Returns a String
     */
    public String getFeilkode() {
        return feilkode;
    }

    /**
     * Sets the feilkode
     * 
     * @param feilkode
     *            The feilkode to set
     */
    public void setFeilkode(String feilkode) {
        this.feilkode = feilkode;
    }

    /**
     * Feilnivå er 0 for alt OK, og 8 for feil.
     * 
     * @return Returns a String
     */
    public String getFeilniva() {
        return feilniva;
    }

    /**
     * Feilnivå er 0 for alt OK, og 8 for feil.
     * 
     * @param feilniva
     *            The feilniva to set
     */
    public void setFeilniva(String feilniva) {
        this.feilniva = feilniva;
    }

    /**
     * Gets the tmpMalpakke
     * 
     * @return Returns a String
     */
    public String getTmpMalpakke() {
        return tmpMalpakke;
    }

    /**
     * Sets the tmpMalpakke
     * 
     * @param tmpMalpakke
     *            The tmpMalpakke to set
     */
    public void setTmpMalpakke(String tmpMalpakke) {
        this.tmpMalpakke = tmpMalpakke;
    }

    /**
     * @return
     */
    public JournalpostVO getJournalpost() {
        return journalpost;
    }

    /**
     * @param journalpostVO
     */
    public void setJournalpost(JournalpostVO journalpostVO) {
        journalpost = journalpostVO;
    }

}
