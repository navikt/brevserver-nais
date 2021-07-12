package no.nav.brevserver.service.dokumentbehandling.support;

import no.nav.brevserver.controller.ControllerBeanFactory;
import no.nav.brevserver.controller.ControllerBi;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.service.dokumentbehandling.to.LagreDokumentRequest;
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
 * Unit tests for DefaultLagreDokumentService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ControllerBeanFactory.class})
public class DefaultLagreDokumentServiceTest {

	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";
	private static final String SYSTEM_ID = "PE2";
	private static final String BRUKER_ID = "brukerID";
	private static final String CONTENT_TYPE_RTF = FilType.RTF.getContentType();
	private static final String KVITTERINGSKOE = "kvitteringsKoe";
	private static final String MALPAKKE = "malpakke";
	private static final byte[] DOKUMENTDATA_RTF = "hello rtf".getBytes();

	@Mock
	private ControllerBi controllerMock;

	@InjectMocks
	private DefaultLagreDokumentService defaultLagreDokumentService;

	@Captor
	private ArgumentCaptor<BrevVO> brevCaptor;

	@Captor
	private ArgumentCaptor<BrevStatusVO> brevStatusCaptor;

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
	public void shouldLagreKladdDokumentRtf() throws Exception {
		defaultLagreDokumentService.lagreDokument(createLagreDokumentRequest(CONTENT_TYPE_RTF));

		verify(controllerMock).lagreDokument(
				brevCaptor.capture(),
				brevStatusCaptor.capture(),
				systemTypeCaptor.capture()
		);

		assertBrev(brevCaptor.getValue(), CONTENT_TYPE_RTF, Konstanter.BREVLAGER_STATUS_KLADD);
		assertBrevStatus(brevStatusCaptor.getValue(), Konstanter.BREVSTATUS_LAGRET_KLADD);
		assertThat(systemTypeCaptor.getValue(), is(SystemType.PE));
	}

	@Test
	public void shouldThrowExceptionIfValidationFails() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("brevStatus must be set");

		defaultLagreDokumentService.lagreDokument(new LagreDokumentRequest());
	}


	@Test
	public void shouldThrowExceptionIfKvitteringskoeMissingAndNewDokumentSet() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("brevStatus.returKoe must be set if newDocument is true");

		LagreDokumentRequest lagreDokumentRequest = createLagreDokumentRequest(CONTENT_TYPE_RTF);
		lagreDokumentRequest.setNewDocument(true);
		lagreDokumentRequest.getBrevStatus().setReturKoe(null);

		defaultLagreDokumentService.lagreDokument(lagreDokumentRequest);
	}

	@Test
	public void shouldThrowExceptionIfMalpakkeMissingAndNewDokumentSet() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("brevStatus.brevmal must be set if newDocument is true");

		LagreDokumentRequest lagreDokumentRequest = createLagreDokumentRequest(CONTENT_TYPE_RTF);
		lagreDokumentRequest.setNewDocument(true);
		lagreDokumentRequest.getBrevStatus().setBrevmal(null);

		defaultLagreDokumentService.lagreDokument(lagreDokumentRequest);
	}

	private void assertBrev(BrevVO brev, String contentType, String lagerStatus) {
		assertThat(brev.getSystemID(), is(SYSTEM_ID));
		assertThat(brev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brev.getContentType(), is(contentType));
		assertThat(brev.getBrevdata(), is(DOKUMENTDATA_RTF));
		assertThat(brev.getBrukerID(), is(BRUKER_ID));
		assertThat(brev.getLagerStatus(), is(lagerStatus));
	}

	private void assertBrevStatus(BrevStatusVO brevStatus, String status) {
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getToken(), is(TOKEN));
		assertThat(brevStatus.getBrevmal(), is(MALPAKKE));
		assertThat(brevStatus.getReturKoe(), is(KVITTERINGSKOE));
		assertThat(brevStatus.getStatus(), is(status));
	}

	private LagreDokumentRequest createLagreDokumentRequest(String contentType) {
		LagreDokumentRequest lagreDokumentRequest = new LagreDokumentRequest();
		lagreDokumentRequest.setBrev(createBrev(contentType));
		lagreDokumentRequest.setBrevStatus(createBrevStatus());
		return lagreDokumentRequest;
	}

	private BrevVO createBrev(String contentType) {
		BrevVO brev = new BrevVO();
		brev.setBrukerID(BRUKER_ID);
		brev.setSystemID(SYSTEM_ID);
		brev.setBrevreferanse(BREVREFERANSE);
		brev.setBrevdata(DOKUMENTDATA_RTF);
		brev.setContentType(contentType);
		return brev;
	}

	private BrevStatusVO createBrevStatus() {
		BrevStatusVO brevStatus = new BrevStatusVO();
		brevStatus.setBrevreferanse(BREVREFERANSE);
		brevStatus.setSystemID(SYSTEM_ID);
		brevStatus.setToken(TOKEN);
		brevStatus.setBrevmal(MALPAKKE);
		brevStatus.setReturKoe(KVITTERINGSKOE);
		return brevStatus;
	}
}
