package no.nav.brevserver.service.search;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Test;

/**
 * Unit tests for SearchObject
 * 
 * These type of objects tend to have alot of side-effects, so there is alot of
 * defensive testing here to ensure the correct object state
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 * 
 */
public class SearchObjectTest {

    @Test
    public void shouldHaveSetStaticSearchFor() {
        assertThat(SearchObject.SEARCH_FOR, is("brevet"));
    }

    @Test
    public void shouldCreateObjectWithConstructor2() {
        SearchObject searchObj = new SearchObject("MyPlace", true);

        assertThat(searchObj.getPlace(), is("MyPlace"));
        assertThat(searchObj.isShallBeHere(), is(true));
        assertDefaultNonConstructorSetFields(searchObj);
    }

    @Test
    public void shouldCreateObjectWithConstructor3() {
        SearchObject searchObj = new SearchObject("MyPlace", true, "MuligFeil");

        assertThat(searchObj.getPlace(), is("MyPlace"));
        assertThat(searchObj.isShallBeHere(), is(true));
        assertThat(searchObj.getMuligFeil(), is("MuligFeil"));
        assertDefaultNonConstructorSetFields(searchObj);
    }

    @Test
    public void shouldGetResultIfSearchObjIsNotChecked() {
        SearchObject searchObj = new SearchObject("Postkassa", true);
        searchObj.setChecked(false); // we are verbose on purpose

        assertThat(searchObj.getResult(), is("Søkte ikke etter brevet i Postkassa"));
    }

    @Test
    public void shouldGetResultIfSearchObjIsCheckedAndWasFoundHere() {
        SearchObject searchObj = new SearchObject("Postkassa", true);
        searchObj.setChecked(true); // we are verbose on purpose
        searchObj.setWasFoundHere(true);

        assertThat(searchObj.getResult(), is("Fant brevet i Postkassa"));
    }

    @Test
    public void shouldGetResultIfSearchObjIsCheckedAndWasNotFoundHere() {
        SearchObject searchObj = new SearchObject("Postkassa", true);
        searchObj.setChecked(true); // we are verbose on purpose
        searchObj.setWasFoundHere(false);

        assertThat(searchObj.getResult(), is("Fant ikke brevet i Postkassa"));
    }

    private void assertDefaultNonConstructorSetFields(SearchObject searchObj) {
        assertThat(searchObj.isChecked(), is(false));
        assertThat(searchObj.isWasFoundHere(), is(false));
        assertThat(searchObj.getStatus(), nullValue());
        assertThat(searchObj.getConclusion(), nullValue());
        assertThat(searchObj.getError(), nullValue());
        assertThat(searchObj.getException(), nullValue());
        assertThat(searchObj.getListOfDokIds(), instanceOf(ArrayList.class));
        assertThat(searchObj.getMsg(), instanceOf(ArrayList.class));
    }
}
