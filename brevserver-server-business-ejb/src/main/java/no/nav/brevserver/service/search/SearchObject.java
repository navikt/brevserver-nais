package no.nav.brevserver.service.search;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rune Røren, Accenture
 */
public class SearchObject {

    static final String SEARCH_FOR = "brevet";

    private String place;

    private boolean checked;
    private boolean wasFoundHere;
    private boolean shallBeHere;

    private String status;
    private String conclusion;
    private String error;
    private String muligFeil;
    private Exception exception;

    private List<String> listOfDokIds = new ArrayList<String>();
    private List<String> msg = new ArrayList<String>();

    public SearchObject(String place, boolean shallBeHere) {
        this.place = place;
        this.shallBeHere = shallBeHere;
    }

    public SearchObject(String place, boolean shallBeHere, String muligFeil) {
        this.place = place;
        this.shallBeHere = shallBeHere;
        this.muligFeil = muligFeil;
    }

    public String getResult() {
        String result = "";
        if (!checked) {
            result = "Søkte ikke etter " + SEARCH_FOR + " i " + place;
        } else {
            result = "Fant " + (wasFoundHere ? "" : "ikke ") + SEARCH_FOR + " i " + place;
        }

        return result;
    }

    public String getStatus() {
        return status;
    }

    public void setMuligFeil(String muligFeil) {
        this.muligFeil = muligFeil;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getConclusion() {
        return conclusion;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public List<String> getListOfDokIds() {
        return listOfDokIds;
    }

    public void setListOfDokIds(List<String> listOfDokIds) {
        this.listOfDokIds = listOfDokIds;
    }

    public List<String> getMsg() {
        return msg;
    }

    public void setMsg(List<String> msg) {
        this.msg = msg;
    }

    public String getPlace() {
        return place;
    }

    public boolean isWasFoundHere() {
        return wasFoundHere;
    }

    public void setWasFoundHere(boolean wasFoundHere) {
        this.wasFoundHere = wasFoundHere;
    }

    public boolean isShallBeHere() {
        return shallBeHere;
    }

    public String getMuligFeil() {
        return muligFeil;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

}
