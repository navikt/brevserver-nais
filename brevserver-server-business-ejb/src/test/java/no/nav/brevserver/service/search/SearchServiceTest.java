package no.nav.brevserver.service.search;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.List;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.service.brevlager.BrevlagerService;
import no.nav.brevserver.service.brevlager.BrevlagerServiceFactory;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfigManager.class, BrevserverServiceFactory.class, BrevlagerServiceFactory.class })
public class SearchServiceTest {

    private static final String SYSTEM_ID = "BI01";
    private static final String BREVREFERANSE = "12345"; // aka brevreferanse
    private static final String LAGERSTATUS = "LagerStatus";
    private static final String BREVMAL = "BrevMal";

    @Mock
    private BrevserverService brevserverServiceMock;
    @Mock
    private BrevlagerService brevlagerServiceMock;

    private SearchService searchService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockConfigManager();
        setupBrevlagerMock();
        setupBrevserverServiceMock();

        searchService = new SearchService();
    }

    @Test
    public void shouldHaveCorrectStateAfterConstructor() {
        List<String> feilRetur = Whitebox.getInternalState(searchService, "feilRetur");

        assertThat(feilRetur.size(), is(1));
        assertThat(feilRetur.iterator().next(), is("Feil (se logg)"));
    }

    @Test
    public void shouldThrowExceptionIfValiderHasMissingBrevreferanse() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Brevreferanse må være satt");

        Whitebox.<Void> invokeMethod(searchService, "valider", SYSTEM_ID, null);
    }

    @Test
    public void shouldThrowExceptionIfValiderHasMissingSystemId() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SystemId må være satt");

        Whitebox.<Void> invokeMethod(searchService, "valider", new Object[] {null, BREVREFERANSE});
    }

    @Test
    public void shouldValidereSystemIdAndDokId() throws Exception {
        Whitebox.<Void> invokeMethod(searchService, "valider", SYSTEM_ID, BREVREFERANSE);
    }

    @Test
    public void shouldHaveExceptionInSearchResultIfBrevlagerIsUnavailableWhenLookinBrevserver() throws Exception {
        when(brevlagerServiceMock.getBrev(SYSTEM_ID, BREVREFERANSE)).thenThrow(
                new BrevTechnicalException("Unavailable Brevlager"));

        SearchObject searchObj = searchService.lookInBrevserver(SYSTEM_ID, BREVREFERANSE);

        verify(brevserverServiceMock, never()).hentBrevStatus(any(String.class), any(String.class));

        assertThat(searchObj.getError(), is("Fikk ikke sjekket brevserver pga. teknisk feil (se logg)"));
        assertThat(searchObj.getException(), instanceOf(BrevTechnicalException.class));
        assertThat(searchObj.getException().getMessage(), is("Unavailable Brevlager"));
    }

    @Test
    public void shouldHaveExceptionInSearchResultIfBrevserverIsUnavailableWhenLookinBrevserver() throws Exception {
        when(brevserverServiceMock.hentBrevStatus(SYSTEM_ID, BREVREFERANSE)).thenThrow(
                new BrevTechnicalException("Unavailable Brevserver"));

        SearchObject searchObj = searchService.lookInBrevserver(SYSTEM_ID, BREVREFERANSE);

        assertThat(searchObj.getError(), is("Fikk ikke sjekket brevserver pga. teknisk feil (se logg)"));
        assertThat(searchObj.getException(), instanceOf(BrevTechnicalException.class));
        assertThat(searchObj.getException().getMessage(), is("Unavailable Brevserver"));
    }

    @Test
    public void shouldLookInBrevserverAndReturnResultForBrevStatusNull() throws Exception {
        when(brevlagerServiceMock.getBrev(SYSTEM_ID, BREVREFERANSE)).thenReturn(createBrev());
        when(brevserverServiceMock.hentBrevStatus(SYSTEM_ID, BREVREFERANSE)).thenReturn(null);

        SearchObject searchObj = searchService.lookInBrevserver(SYSTEM_ID, BREVREFERANSE);

        assertThat(searchObj.isWasFoundHere(), is(true));
        assertThat(searchObj.getStatus(), is("Brevet er ikke mottatt av brevserver. Har BISYS oversendt brevet ?"));
        assertThat(searchObj.getConclusion(), nullValue());
        assertAndVerifyCommonResult(searchObj);
    }

    @Test
    public void shouldLookInBrevserverAndReturnResultForBrevStatusBrevpakke() throws Exception {
        when(brevlagerServiceMock.getBrev(SYSTEM_ID, BREVREFERANSE)).thenReturn(createBrev());
        when(brevserverServiceMock.hentBrevStatus(SYSTEM_ID, BREVREFERANSE)).thenReturn(
                createBrevstatus(Konstanter.BREVSTATUS_BREVPAKKE));

        SearchObject searchObj = searchService.lookInBrevserver(SYSTEM_ID, BREVREFERANSE);

        assertThat(searchObj.isWasFoundHere(), is(true));
        assertThat(
                searchObj.getStatus(),
                is("Brevet har status BREVPAKK og mal BrevMal i brevserver. Brevet har status LagerStatus i brevlageret"));
        assertThat(searchObj.getConclusion(), is("Brevet er sendt til Dialogue for oppretting."));
        assertAndVerifyCommonResult(searchObj);
    }

    @Test
    public void shouldLookInBrevserverAndReturnResultForBrevStatusUtskrift() throws Exception {
        when(brevlagerServiceMock.getBrev(SYSTEM_ID, BREVREFERANSE)).thenReturn(createBrev());
        when(brevserverServiceMock.hentBrevStatus(SYSTEM_ID, BREVREFERANSE)).thenReturn(
                createBrevstatus(Konstanter.BREVSTATUS_UTSKRIFT));

        SearchObject searchObj = searchService.lookInBrevserver(SYSTEM_ID, BREVREFERANSE);

        assertThat(searchObj.isWasFoundHere(), is(true));
        assertThat(
                searchObj.getStatus(),
                is("Brevet har status UTSKRIFT og mal BrevMal i brevserver. Brevet har status LagerStatus i brevlageret"));
        assertThat(searchObj.getConclusion(), is("Brevet er sendt til RTF-Konverter for ferdigstilling"));
        assertAndVerifyCommonResult(searchObj);
    }

    @Test
    public void shouldLookInBrevserverAndReturnResultForBrevStatusHosBrevklient() throws Exception {
        when(brevlagerServiceMock.getBrev(SYSTEM_ID, BREVREFERANSE)).thenReturn(createBrev());
        when(brevserverServiceMock.hentBrevStatus(SYSTEM_ID, BREVREFERANSE)).thenReturn(
                createBrevstatus(Konstanter.BREVSTATUS_HOS_BREVKLIENT));

        SearchObject searchObj = searchService.lookInBrevserver(SYSTEM_ID, BREVREFERANSE);

        assertThat(searchObj.isWasFoundHere(), is(true));
        assertThat(searchObj.getStatus(),
                is("Brevet har status KLIENT og mal BrevMal i brevserver. Brevet har status LagerStatus i brevlageret"));
        assertThat(searchObj.getConclusion(),
                is("Saksbehandler redigerer brevet eller har opplevd feil mens brevet redigeres"));
        assertAndVerifyCommonResult(searchObj);
    }

    @Test
    public void shouldLookInBrevserverAndReturnResultForNoBrevInBrevlagerAndBrevpakkeInBrevserver() throws Exception {
        when(brevlagerServiceMock.getBrev(SYSTEM_ID, BREVREFERANSE)).thenReturn(null);
        when(brevserverServiceMock.hentBrevStatus(SYSTEM_ID, BREVREFERANSE)).thenReturn(
                createBrevstatus(Konstanter.BREVSTATUS_BREVPAKKE));

        SearchObject searchObj = searchService.lookInBrevserver(SYSTEM_ID, BREVREFERANSE);

        verify(brevlagerServiceMock).getBrev(SYSTEM_ID, BREVREFERANSE);
        verify(brevserverServiceMock).hentBrevStatus(SYSTEM_ID, BREVREFERANSE);

        assertThat(searchObj.isWasFoundHere(), is(false));
        assertThat(searchObj.getStatus(), is("Brevet har status BREVPAKK og mal BrevMal i brevserver"));
        assertThat(searchObj.getConclusion(), is("Brevet er sendt til Dialogue for oppretting."));
        assertAndVerifyCommonResult(searchObj);
    }

    private void assertAndVerifyCommonResult(SearchObject searchObj) throws Exception {
        verify(brevlagerServiceMock).getBrev(SYSTEM_ID, BREVREFERANSE);
        verify(brevserverServiceMock).hentBrevStatus(SYSTEM_ID, BREVREFERANSE);

        assertThat(searchObj.getPlace(), is("Brevlageret"));
        assertThat(searchObj.isShallBeHere(), is(true));
        assertThat(searchObj.isChecked(), is(true));
        assertThat(searchObj.getError(), nullValue());
        assertThat(searchObj.getException(), nullValue());
    }

    @Test
    public void shouldFindDocument() throws Exception {
        // we do not look in the log files
        List<SearchObject> searchObjects = searchService.findDocument(SYSTEM_ID, BREVREFERANSE);

        assertThat(searchObjects.size(), is(2));
        assertThat(searchObjects.get(0).getPlace(), is("Brevlageret"));
        assertThat(searchObjects.get(1).getPlace(), is("loggen"));
    }

    @Test
    public void shouldReturnNullIfAnalyzedIsCalledWithNull() {
        assertThat(searchService.analyze(null), nullValue());
    }

    @Test
    public void shouldReturnResultOfAnalyzationTestCase1() {
        List<SearchObject> searchObjects = new ArrayList<SearchObject>();
        searchObjects.add(createSearchObjectForAnalyze("Place1", false, true, false, null, "Error", "Status",
                "Conclusion"));

        List<String> analyzed = searchService.analyze(searchObjects);

        assertThat(analyzed.get(0), is("Søkte ikke etter brevet i Place1"));
        assertThat(analyzed.get(1), is("Feil   : Error"));
        assertThat(analyzed.get(2), is("Status : Status"));
        assertThat(analyzed.get(3), is("Konkl. : Conclusion"));
        assertThat(analyzed.get(4), is("   MyMessage"));
    }

    @Test
    public void shouldReturnResultOfAnalyzationTestCase2() {
        List<SearchObject> searchObjects = new ArrayList<SearchObject>();
        searchObjects.add(createSearchObjectForAnalyze("Place2", true, true, false, "MuligFeil", null, null, null));

        List<String> analyzed = searchService.analyze(searchObjects);

        assertThat(analyzed.get(0), is("FUNN! Brevet ble funnet i Place2. MuligFeil"));
        assertThat(analyzed.get(1), is("   MyMessage"));
    }

    @Test
    public void shouldReturnResultOfAnalyzationTestCase3() {
        List<SearchObject> searchObjects = new ArrayList<SearchObject>();
        searchObjects.add(createSearchObjectForAnalyze("Place3", true, false, false, null, null, null, null));

        List<String> analyzed = searchService.analyze(searchObjects);

        assertThat(analyzed.get(0), is("Brevet ble ikke funnet i Place3. Det er bra!"));
        assertThat(analyzed.get(1), is("   MyMessage"));
    }

    private SearchObject createSearchObjectForAnalyze(String place, boolean checked, boolean wasFoundHere,
            boolean shallBeHere, String muligFeil, String error, String status, String conclusion) {
        SearchObject searchObj = new SearchObject(place, shallBeHere);
        searchObj.setChecked(checked);
        searchObj.setWasFoundHere(wasFoundHere);
        searchObj.setMuligFeil(muligFeil);
        searchObj.setError(error);
        searchObj.setStatus(status);
        searchObj.setConclusion(conclusion);
        searchObj.getMsg().add("MyMessage");
        return searchObj;
    }

    @Test
    public void shouldReturnCorrectSystemId() throws Exception {
        invokeAndAssertSystemId("BI01", "BISYS");
        invokeAndAssertSystemId("OB05", "Predator");
        invokeAndAssertSystemId("IT05", "Infotrygd");
        invokeAndAssertSystemId("AS11", "eSak");
        invokeAndAssertSystemId("PE2", "PESYS");
        invokeAndAssertSystemId("AUTOSYS", "dette ukjente systemet");
    }

    private void invokeAndAssertSystemId(String systemId, String expectedSystemName) throws Exception {
        String actualSystemName = Whitebox.<String> invokeMethod(searchService, "getSystemName", systemId);

        assertThat(actualSystemName, is(expectedSystemName));
    }

    private BrevVO createBrev() {
        BrevVO brev = new BrevVO();
        brev.setLagerStatus(LAGERSTATUS);
        return brev;
    }

    private BrevStatusVO createBrevstatus(String status) {
        BrevStatusVO brevStatus = new BrevStatusVO();
        brevStatus.setStatus(status);
        brevStatus.setBrevmal(BREVMAL);
        return brevStatus;
    }

    private void mockConfigManager() {
        ConfigManager configManagerMock = mock(ConfigManager.class);
        mockStatic(ConfigManager.class);
        when(ConfigManager.getInstance()).thenReturn(configManagerMock);
    }

    private void setupBrevserverServiceMock() {
        mockStatic(BrevserverServiceFactory.class);
        BrevserverServiceFactory brevserverServiceFactoryMock = mock(BrevserverServiceFactory.class);
        when(BrevserverServiceFactory.getInstance()).thenReturn(brevserverServiceFactoryMock);
        when(brevserverServiceFactoryMock.createBrevserverService()).thenReturn(brevserverServiceMock);
    }

    private void setupBrevlagerMock() throws Exception {
        mockStatic(BrevlagerServiceFactory.class);
        BrevlagerServiceFactory brevlagerServiceFactoryMock = mock(BrevlagerServiceFactory.class);
        when(BrevlagerServiceFactory.getInstance()).thenReturn(brevlagerServiceFactoryMock);
        when(brevlagerServiceFactoryMock.createBrevlagerService()).thenReturn(brevlagerServiceMock);
    }
}
