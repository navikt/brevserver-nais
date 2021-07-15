package no.nav.brevserver.service.dokumentbehandling.support;

import no.nav.brevserver.controller.ControllerBeanFactory;
import no.nav.brevserver.controller.ControllerBi;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Unit tests for DefaultFerdigstillDokumentServiceTest
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ControllerBeanFactory.class})
public class DefaultFerdigstillDokumentServiceTest {

	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";
	private static final String SYSTEM_ID = "PE2";
	private static final String BRUKER_ID = "brukerID";
	private static final String KVITTERINGSKOE = "kvitteringsKoe";
	private static final String MALPAKKE = "malpakke";
	private static final boolean NYTT_DOKUMENT = true;
	private static final String CONTENT_TYPE_PDF = FilType.PDF.getContentType();
	private static final String CONTENT_TYPE_RTF = FilType.RTF.getContentType();
	private static final byte[] DOKUMENTDATA_PDF = "hello pdf".getBytes();
	private static final byte[] DOKUMENTDATA_RTF = "hello rtf".getBytes();

	@Mock
	private ControllerBi controllerMock;

	@InjectMocks
	private DefaultFerdigstillDokumentService defaultFerdigstillDokumentService;

	@Captor
	private ArgumentCaptor<BrevStatusVO> brevStatusVOCaptor;

	@Captor
	private ArgumentCaptor<BrevVO> brevCaptor;

	@Captor
	private ArgumentCaptor<SystemType> systemTypeCaptor;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		setupControllerMock();
	}

	private void setupControllerMock() throws Exception {
		mockStatic(ControllerBeanFactory.class);
		ControllerBeanFactory controllerBeanFactoryMock = mock(ControllerBeanFactory.class);
		when(ControllerBeanFactory.getInstance()).thenReturn(controllerBeanFactoryMock);
		when(controllerBeanFactoryMock.getController()).thenReturn(controllerMock);
	}

	@Test
	public void shouldFerdigstilleDokument() throws BrevException {
		defaultFerdigstillDokumentService.ferdigstillDokument(createFerdigstillDokumentRequest());

		verify(controllerMock).ferdigstillDokument(brevStatusVOCaptor.capture(),
				brevCaptor.capture(), brevCaptor.capture(), systemTypeCaptor.capture());

		assertBrevStatus(brevStatusVOCaptor.getValue());
		assertRtfBrev(brevCaptor.getAllValues().get(0));
		assertPdfBrev(brevCaptor.getAllValues().get(1));
		assertThat(systemTypeCaptor.getValue(), is(SystemType.PE));
	}

	@Test
	public void shouldThrowExceptionIfValidationFails() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("brevStatus must be set");

		defaultFerdigstillDokumentService.ferdigstillDokument(new FerdigstillDokumentRequest());
	}

	@Test
	public void shouldThrowExceptionIfKvitteringskoeMissingAndNewDokumentSet() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("brevStatus.returKoe must be set if newDocument is true");

		FerdigstillDokumentRequest ferdigstillDokumentRequest = createFerdigstillDokumentRequest();
		ferdigstillDokumentRequest.setNewDocument(true);
		ferdigstillDokumentRequest.getBrevStatus().setReturKoe(null);

		defaultFerdigstillDokumentService.ferdigstillDokument(ferdigstillDokumentRequest);
	}

	@Test
	public void shouldThrowExceptionIfMalpakkeMissingAndNewDokumentSet() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("brevStatus.brevmal must be set if newDocument is true");

		FerdigstillDokumentRequest ferdigstillDokumentRequest = createFerdigstillDokumentRequest();
		ferdigstillDokumentRequest.setNewDocument(true);
		ferdigstillDokumentRequest.getBrevStatus().setBrevmal(null);

		defaultFerdigstillDokumentService.ferdigstillDokument(ferdigstillDokumentRequest);
	}

	private void assertBrevStatus(BrevStatusVO brevStatus) {
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getToken(), is(TOKEN));
		assertThat(brevStatus.getReturKoe(), is(KVITTERINGSKOE));
		assertThat(brevStatus.getBrevmal(), is(MALPAKKE));
	}

	private void assertRtfBrev(BrevVO brev) {
		assertThat(brev.getBrevdata(), is(DOKUMENTDATA_RTF));
		assertThat(brev.getContentType(), is(CONTENT_TYPE_RTF));
		assertBrev(brev);
	}

	private void assertPdfBrev(BrevVO brev) {
		assertThat(brev.getBrevdata(), is(DOKUMENTDATA_PDF));
		assertThat(brev.getContentType(), is(CONTENT_TYPE_PDF));
		assertBrev(brev);
	}

	private void assertBrev(BrevVO brev) {
		assertThat(brev.getBrukerID(), is(BRUKER_ID));
		assertThat(brev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brev.getSystemID(), is(SYSTEM_ID));
	}

	private FerdigstillDokumentRequest createFerdigstillDokumentRequest() {
		FerdigstillDokumentRequest ferdigstillDokumentRequest = new FerdigstillDokumentRequest();
		ferdigstillDokumentRequest.setBrevStatus(createBrevStatus());
		ferdigstillDokumentRequest.setBrev(createBrev(DOKUMENTDATA_RTF, CONTENT_TYPE_RTF));
		ferdigstillDokumentRequest.setPdfBrev(createBrev(DOKUMENTDATA_PDF, CONTENT_TYPE_PDF));
		ferdigstillDokumentRequest.setNewDocument(NYTT_DOKUMENT);
		return ferdigstillDokumentRequest;
	}

	private BrevStatusVO createBrevStatus() {
		BrevStatusVO brevStatus = new BrevStatusVO();
		brevStatus.setSystemID(SYSTEM_ID);
		brevStatus.setBrevreferanse(BREVREFERANSE);
		brevStatus.setToken(TOKEN);
		brevStatus.setBrevmal(MALPAKKE);
		brevStatus.setReturKoe(KVITTERINGSKOE);
		return brevStatus;
	}

	private BrevVO createBrev(byte[] dokumentData, String contentType) {
		BrevVO brev = new BrevVO();
		brev.setBrukerID(BRUKER_ID);
		brev.setSystemID(SYSTEM_ID);
		brev.setBrevreferanse(BREVREFERANSE);
		brev.setBrevdata(dokumentData);
		brev.setContentType(contentType);
		return brev;
	}
}
