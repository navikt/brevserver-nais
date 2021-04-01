package no.nav.brevserver.service.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.file.FileDateComparator;
import no.nav.brevserver.server.common.file.FileUtil;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.utility.ArgumentValidator;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.service.brevlager.BrevlagerService;
import no.nav.brevserver.service.brevlager.BrevlagerServiceFactory;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;

/**
 * Tjeneste som leter etter brev i Brevlager og ulike MQ-køer
 * 
 * @author Rune Røren, Accenture
 */
public class SearchService {

    public static final String PLACE = "_PLACE";
    
    private static final String FEIL_INGEN_LOGGFILER_DEFINERT = "Ingen logg-filer er definert";
    private static final String FEIL_INGEN_LOGGFILER_FUNNET = "Ingen logg-filer ble funnet";
    private static final int SEARCH_MAX_HITS = 200;
    
    Log log = new Log(this.getClass());

    private List<String> feilRetur = new ArrayList<String>();

    protected SearchService() {
        feilRetur.add("Feil (se logg)");
    }

    /**
     * Returnerer en List med SearchObjects for ulike steder brevet kan være
     * 
     * @param args
     *            Brevargumenter
     * @return
     */
    public List<SearchObject> findDocument(String systemId, String brevreferanse) {
        valider(systemId, brevreferanse);

        String methSig = "SearchService.findDocument(" + systemId + ":" + brevreferanse + ")";
        PerformanceLogger p = new PerformanceLogger(methSig);
        List<SearchObject> result = new ArrayList<SearchObject>();

        result.add(lookInBrevserver(systemId, brevreferanse));
        result.add(lookInLogfiles(systemId, brevreferanse));

        p.stop();

        return result;
    }

    /**
     * Returnerer en list med Strings med info om hvor brevet kan være
     * 
     * @param listOfSearchObjects
     * @return The list
     */
    public List<String> analyze(List<SearchObject> listOfSearchObjects) {
        if (listOfSearchObjects == null) {
            return null;
        }

        List<String> result = new ArrayList<String>();

        for (int i = 0; i < listOfSearchObjects.size(); i++) {
            SearchObject searchObject = (SearchObject) listOfSearchObjects.get(i);

            if (!searchObject.isChecked()) {
                result.add("Søkte ikke etter brevet i " + searchObject.getPlace());
            } else {
                if (searchObject.isWasFoundHere()) {
                    String msg1 = searchObject.getMuligFeil() != null ? searchObject.getMuligFeil() : "";
                    String msg2 = searchObject.isShallBeHere() ? "Det er bra!" : msg1;

                    result.add("FUNN! Brevet ble funnet i " + searchObject.getPlace() + ". " + msg2);
                } else {
                    String msg1 = searchObject.getMuligFeil() != null ? searchObject.getMuligFeil() : "";
                    String msg2 = !searchObject.isShallBeHere() ? "Det er bra!" : msg1;

                    result.add("Brevet ble ikke funnet i " + searchObject.getPlace() + ". " + msg2);
                }
            }

            if (searchObject.getError() != null) {
                result.add("Feil   : " + searchObject.getError());
            }
            if (searchObject.getStatus() != null) {
                result.add("Status : " + searchObject.getStatus());
            }
            if (searchObject.getConclusion() != null) {
                result.add("Konkl. : " + searchObject.getConclusion());
            }

            if (searchObject.getMsg().size() > 0) {
                for (int j = 0; j < searchObject.getMsg().size(); j++) {
                    String msg = "   " + (String) searchObject.getMsg().get(j);
                    result.add(msg);
                }
            }
        }

        return result;
    }

    /**
     * Leter i brevserver og brevlager
     * 
     * @param args
     * @return søkeobjekt
     */
    public SearchObject lookInBrevserver(String systemId, String brevreferanse) {
        valider(systemId, brevreferanse);

        String methSig = "SearchService.lookInBrevserver(" + systemId + ":" + brevreferanse + ")";
        PerformanceLogger p = new PerformanceLogger(methSig);

        SearchObject result = new SearchObject("Brevlageret", true);

        try {
            String brevlagerStatus = "";
            // Sjekk brevlager
            BrevlagerService brevlager = BrevlagerServiceFactory.getInstance().createBrevlagerService();
            BrevVO brev = brevlager.getBrev(systemId, brevreferanse);

            result.setWasFoundHere(brev != null);
            result.setChecked(true);
            if (brev != null) {
                brevlagerStatus = ". Brevet har status " + brev.getLagerStatus() + " i brevlageret";
            }

            // Sjekk status på brevet
            BrevserverService brevlagerService = BrevserverServiceFactory.getInstance().createBrevserverService();
            BrevStatusVO status = brevlagerService.hentBrevStatus(systemId, brevreferanse);

            if (status != null) {
                result.setStatus("Brevet har status " + status.getStatus() + " og mal " + status.getBrevmal()
                        + " i brevserver" + brevlagerStatus);

                if (Konstanter.BREVSTATUS_BREVPAKKE.equals(status.getStatus())) {
                    result.setConclusion("Brevet er sendt til Dialogue for oppretting.");

                } else if (Konstanter.BREVSTATUS_UTSKRIFT.equals(status.getStatus())) {
                    result.setConclusion("Brevet er sendt til RTF-Konverter for ferdigstilling");

                } else if (Konstanter.BREVSTATUS_HOS_BREVKLIENT.equals(status.getStatus())) {
                    result.setConclusion("Saksbehandler redigerer brevet eller har opplevd feil mens brevet redigeres");
                }
            } else {
                result.setStatus("Brevet er ikke mottatt av brevserver. Har " + getSystemName(systemId)
                        + " oversendt brevet ?");
            }

        } catch (BrevTechnicalException e) {
            result.setError("Fikk ikke sjekket brevserver pga. teknisk feil (se logg)");
            result.setException(e);
            log.error(methSig, result.getError(), e);
        }

        p.stop();

        return result;
    }

    private String getSystemName(String systemId) {
        if ("BI01".equals(systemId)) {
            return "BISYS";

        } else if ("OB05".equals(systemId)) {
            return "Predator";

        } else if ("IT05".equals(systemId)) {
            return "Infotrygd";

        } else if ("AS11".equals(systemId)) {
            return "eSak";

        } else if ("PE2".equals(systemId)) {
            return "PESYS";
        } else {
            return "dette ukjente systemet";
        }
    }

    private SearchObject lookInLogfiles(String systemId, String brevreferanse) {
        String methSig = "lookInLogfiles(" + systemId + ":" + brevreferanse + ")";

        boolean lookInLogfiles = ConfigManager.getInstance().getBool("SearchService.lookinlogfiles", false);
        String logdir = ConfigManager.getInstance().getString("SearchService.logdir", null);
        String logPrefix = ConfigManager.getInstance().getString("SearchService.logfile", null);
        int maxhits = ConfigManager.getInstance().getInt("SearchService.maxhits", SEARCH_MAX_HITS);

        SearchObject result = new SearchObject("loggen", true, null);

        if (!lookInLogfiles) {
            return result;
        }

        valider(systemId, brevreferanse);

        if (logdir == null || logPrefix == null) {
            log.error(methSig, FEIL_INGEN_LOGGFILER_DEFINERT);
            result.setError(FEIL_INGEN_LOGGFILER_DEFINERT);
            return result;
        }

        String[] logfiles = getLogfiles(logdir, logPrefix);
        if (logfiles == null) {
            log.error(methSig, FEIL_INGEN_LOGGFILER_FUNNET);
            result.setError(FEIL_INGEN_LOGGFILER_FUNNET);
            return result;
        }

        String searchFor = brevreferanse.toLowerCase();
        int hits = 0;

        for (int i = 0; i < logfiles.length; i++) {

            if (hits >= maxhits) {
                result.getMsg().add("Fikk over " + Integer.toString(maxhits) + " treff...");
                break;
            }

            String logFile = logfiles[i];

            BufferedReader in = null;
            File copy = null;

            try {
                File file = new File(logFile);
                String name = " (" + file.getName() + ")";
                copy = File.createTempFile("TempLog", file.getName());

                FileUtil.copy(file, copy);

                FileReader reader = new FileReader(copy);
                in = new BufferedReader(reader);

                String line = decode(in.readLine());
                while (line != null && hits <= maxhits) {
                    String linel = line.toLowerCase();

                    boolean isSecurityAccepted = validateForViewing(linel);

                    if (linel.indexOf(searchFor) > -1 && isSecurityAccepted) {
                        result.getMsg().add(line + name);
                        result.setWasFoundHere(true);
                        hits++;
                    }
                    line = decode(in.readLine());
                }

            } catch (IOException e) {
                String errMsg = "Feil ved lesing fra filen " + logFile;
                result.setError(errMsg);
                log.error(methSig, errMsg, e);

            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error(methSig, "Fikk ikke lukke input-stream", e);
                    }
                }

                // Slett kopi
                if (copy != null) {
                    copy.delete();
                }
            }
        }

        result.setChecked(true);

        return result;
    }

    private String[] getLogfiles(final String logdir, final String logname) {
        File file1 = new File(logdir);

        File[] files = file1.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                String name = pathname.getName();
                if (name != null && name.indexOf(logname) >= 0) {
                    return true;
                }
                return false;
            }
        });

        Arrays.sort(files, new FileDateComparator());

        String[] result = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            result[i] = files[i].getAbsolutePath();
        }

        return result;
    }

    private boolean validateForViewing(String s) {
        boolean doValidation = ConfigManager.getInstance().getBool("SearchService.doValidation", true);

        if (!doValidation) {
            return true;
        }

        if (s != null && s.indexOf("token") == -1 && s.indexOf("passord") == -1) {
            return true;
        }

        return false;
    }

    private String decode(String strEBCDIC) {
        if (strEBCDIC == null) {
            return null;
        }
        boolean doDecode = ConfigManager.getInstance().getBool("SearchService.decode", false);
        if (!doDecode) {
            return strEBCDIC;
        }

        String charset = ConfigManager.getInstance().getString("SearchService.decode.charset", "ISO-8859-1");

        Charset cSet = Charset.forName(charset);
        ByteBuffer bb = ByteBuffer.wrap(strEBCDIC.getBytes());
        CharBuffer cb = cSet.decode(bb);
        return cb.toString();
    }

    private void valider(String systemId, String brevreferanse) {
        ArgumentValidator.isNotNull("SystemId må være satt", systemId);
        ArgumentValidator.isNotNull("Brevreferanse må være satt", brevreferanse);
    }
}
