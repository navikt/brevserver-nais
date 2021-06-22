package no.nav.brevserver.service.queue.xml.beans;

import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.vo.AvsenderMottakerVO;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.DokBeskrivelseVO;
import no.nav.brevserver.server.common.vo.JournalpostVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.TilleggsinfoVO;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Klasse som håndterer alle SAX eventer
 * som finnes i brevløsningen for BI(drag).
 */
public class XMLHandler extends DefaultHandler {
    protected KvitteringVO kvittering = new KvitteringVO();

    protected static final String EMPTY_STRING = "";

    BrevStatusVO brevStatusVo = new BrevStatusVO();

    String thisElement = EMPTY_STRING;
    String nameSpace = EMPTY_STRING;

    private JournalpostVO journal = null;
    private AvsenderMottakerVO avsenderMottaker = null;
    private TilleggsinfoVO tilleggsinfo = null;
    private DokBeskrivelseVO dokBeskrivelse = null;

    private Log log = new Log(this.getClass());

    public void startDocument() throws SAXException {
        //
    }

    /**
     * Mottar melding om start i et element
     *
     * @param namespaceURI
     * @param localName
     * @param qName
     * @param atts
     * @throws SAXException
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

        if (localName.equalsIgnoreCase("rtv-brev")) {
            // online
            brevStatusVo.setToken(atts.getValue(nameSpace, "klientToken"));
            brevStatusVo.setBestillerBrukerID(atts.getValue(nameSpace, "saksbehandler"));
            brevStatusVo.setBrevmal(atts.getValue(nameSpace, "malpakke"));
            brevStatusVo.setSystemID(atts.getValue(nameSpace, "sysid"));
            brevStatusVo.setModus(atts.getValue(nameSpace, "modus"));
            brevStatusVo.setFormat(atts.getValue(nameSpace, "format"));
            brevStatusVo.setSkrivertype(atts.getValue(nameSpace, "skrivertype"));
            brevStatusVo.setSkriver(atts.getValue(nameSpace, "skriver"));
            brevStatusVo.setArkiver(atts.getValue(nameSpace, "arkiver"));
            brevStatusVo.setSkuff(atts.getValue(nameSpace, "skuff"));

            brevStatusVo.setPassord(atts.getValue(nameSpace, "passord"));

        } else if (localName.equalsIgnoreCase("brev")) {
            brevStatusVo.setBrevreferanse(atts.getValue(nameSpace, "brevref"));

        } else if (localName.equalsIgnoreCase("rtv-brevutskrift")) {
            // batch RTF
            brevStatusVo.setBrevreferanse(atts.getValue(nameSpace, "brevref"));
            brevStatusVo.setSystemID(atts.getValue(nameSpace, "sysid"));
        }

        thisElement = qName;

        // Journalpost
        if (localName.equalsIgnoreCase("jPost")) {
            journal = new JournalpostVO();
            kvittering.setJournalpost(journal);

        } else if (journal != null) {
            if (localName.equalsIgnoreCase("jAvsMot")) {
                avsenderMottaker = new AvsenderMottakerVO();
                journal.addAvsenderMottaker(avsenderMottaker);

            } else if (localName.equalsIgnoreCase("jTilleggsinfo")) {
                tilleggsinfo = new TilleggsinfoVO();

                String tiType = atts.getValue(nameSpace, "tiType");
                String tiVerdi = atts.getValue(nameSpace, "tiVerdi");

                tilleggsinfo.setType(tiType);
                tilleggsinfo.setVerdi(tiVerdi);

                boolean uniqueId = "BIDRNR".equals(tiType) || "INKNR".equals(tiType);
                boolean okToSetnewId = (kvittering.getBrevreferanse() == null && brevStatusVo.getBrevreferanse() == null);

                if (uniqueId && okToSetnewId) {
                    kvittering.setBrevreferanse(tiType + ":" + tiVerdi);
                    brevStatusVo.setBrevreferanse(tiType + ":" + tiVerdi);
                }

                journal.addTilleggsinfo(tilleggsinfo);

            } else if (localName.equalsIgnoreCase("jDokBeskrivelse")) {
                dokBeskrivelse = new DokBeskrivelseVO();

                dokBeskrivelse.setKategori(atts.getValue(nameSpace, "dbKategori"));
                dokBeskrivelse.setStatus(atts.getValue(nameSpace, "dbStatus"));

                journal.addDokBeskrivelse(dokBeskrivelse);

            } else if (dokBeskrivelse != null && localName.equalsIgnoreCase("jDokVersjon")) {
                dokBeskrivelse.setVersjon(atts.getValue(nameSpace, "dvVersjon"));
                dokBeskrivelse.setVariant(atts.getValue(nameSpace, "dvVariant"));
                dokBeskrivelse.setLagringsFormat(atts.getValue(nameSpace, "dvLagrFormat"));
                dokBeskrivelse.setLagringsEnhet(atts.getValue(nameSpace, "dvLagrEnhet"));
            }
        }
    }

    /**
     * Mottar melding om karakterdata inne i et element.
     *
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (thisElement == null || EMPTY_STRING.equals(thisElement)) {
            return;
        }

        String value = new String(ch, start, length).trim();

        if ("brevref".equalsIgnoreCase(thisElement)) {
            kvittering.setBrevreferanse(value);
            return;
        }
        if ("sysid".equalsIgnoreCase(thisElement)) {
            kvittering.setSystemID(value);
            return;
        }
        if ("type".equalsIgnoreCase(thisElement)) {
            kvittering.setContentType(value);
            return;
        }

        if ("feilniva".equalsIgnoreCase(thisElement)) {
            kvittering.setFeilniva(value);
            return;
        }

        if ("feilkode".equalsIgnoreCase(thisElement)) {
            kvittering.setFeilkode(value);
            return;
        }
        if ("malpakke".equalsIgnoreCase(thisElement)) {
            kvittering.setTmpMalpakke(value);
            return;
        }

        // Felter i en journalpost
        if (journal != null) {
            boolean mapOk = mapJournalPost(thisElement, value);

            // AvsenderMottaker
            if (!mapOk && avsenderMottaker != null) {
                mapAvsenderMottaker(thisElement, value);
            }

        }
    }

    private boolean mapJournalPost(String element, String value) {
        if ("jDato".equalsIgnoreCase(element)) {
            journal.setDato(value);
            return true;
        }

        if ("jDokType".equalsIgnoreCase(element)) {
            journal.setDokType(value);
            return true;
        }

        if ("jStatus".equalsIgnoreCase(element)) {
            journal.setStatus(value);
            return true;
        }

        if ("jInnhBeskr".equalsIgnoreCase(element)) {
            journal.setInnhBeskr(value);
            return true;
        }

        if ("jInnhBeskrUoff".equalsIgnoreCase(element)) {
            journal.setInnhBeskrUoff(value);
        }

        if ("jUDatertDok".equalsIgnoreCase(element)) {
            journal.setUDatertDok(value);
            return true;
        }

        if ("jTilgangsgruppe".equalsIgnoreCase(element)) {
            journal.setTilgangsgruppe(value);
            return true;
        }

        if ("jTilgangskode".equalsIgnoreCase(element)) {
            journal.setTilgangskode(value);
            return true;
        }

        if ("jHjemmelUoff".equalsIgnoreCase(element)) {
            journal.setHjemmelUoff(value);
            return true;
        }

        return false;
    }

    private boolean mapAvsenderMottaker(String element, String value) {
        if ("amInnholdstype".equalsIgnoreCase(element)) {
            avsenderMottaker.setInnholdstype(value);
            return true;
        }

        if ("amKopi".equalsIgnoreCase(element)) {
            avsenderMottaker.setKopi(value);
            return true;
        }

        if ("amBehAns".equalsIgnoreCase(element)) {
            avsenderMottaker.setBehAns(value);
            return true;
        }

        if ("amFornavn".equalsIgnoreCase(element)) {
            avsenderMottaker.setFornavn(value);
            return true;
        }

        if ("amEtternavn".equalsIgnoreCase(element)) {
            avsenderMottaker.setEtternavn(value);
            return true;
        }

        if ("amAvsMottUoff".equalsIgnoreCase(element)) {
            avsenderMottaker.setAvsMottUoff(value);
            return true;
        }

        if ("amSaksbehAdmEnhet".equalsIgnoreCase(element)) {
            avsenderMottaker.setSaksbehAdmEnhet(value);
            return true;
        }

        if ("amSaksbehIdent".equalsIgnoreCase(element)) {
            avsenderMottaker.setSaksbehIdent(value);
            return true;
        }

        if ("amForsendMaate".equalsIgnoreCase(element)) {
            avsenderMottaker.setForsendMaate(value);
            return true;
        }

        if ("amForkortSaksbehEnhet".equalsIgnoreCase(element)) {
            avsenderMottaker.setForkortSaksbehEnhet(value);
            return true;
        }

        if ("amSaksbehInit".equalsIgnoreCase(element)) {
            avsenderMottaker.setSaksbehInit(value);
            return true;
        }

        if ("amJournalEnhet".equalsIgnoreCase(element)) {
            avsenderMottaker.setJournalEnhet(value);
            return true;
        }

        if ("amAvskrivMaate".equalsIgnoreCase(element)) {
            avsenderMottaker.setAvskrivMaate(value);
            return true;
        }

        if ("amAvskrivDato".equalsIgnoreCase(element)) {
            avsenderMottaker.setAvskrivDato(value);
            return true;
        }

        return false;
    }

    /**
     * Mottar errormelding fra parseren om feil som er håndterbare (recoverable)
     *
     * @param e
     */
    public void error(SAXParseException e) {
        log.error("XMLHandler.error()", "Feil med XML", e);
    }

    /**
     * Mottar warnings fra parseren, forsøker å logge feilen
     *
     * @param e
     */
    public void warning(SAXParseException e) {
        log.warning("XMLHandler.warning()", "Feil med XML", e);
    }

    /**
     * Mottar fatalErrors fra parseren, forsøker å logge feilen
     *
     * @param e
     */
    public void fatalError(SAXParseException e) {
        // log.error("XMLHandler.fatalError()", "Feil med XML", e);  // Logges høyere opp i hierarkiet
    }

    /**
     * Mottar melding om slutten av et element.
     *
     * @param namespaceURI
     * @param localName
     * @param qName
     * @throws SAXException
     */
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        thisElement = EMPTY_STRING;
    }

    public KvitteringVO getKvittering() {
        return kvittering;
    }

    public BrevStatusVO getBrevStatus() {
        return brevStatusVo;
    }
}