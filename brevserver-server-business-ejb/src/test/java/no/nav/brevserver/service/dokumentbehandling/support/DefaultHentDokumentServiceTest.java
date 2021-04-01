package no.nav.brevserver.service.dokumentbehandling.support;

import no.nav.brevserver.controller.ControllerBeanFactory;
import no.nav.brevserver.controller.ControllerBi;
import no.nav.brevserver.server.common.config.KnappStatus;
import no.nav.brevserver.server.common.exception.BrevRuntimeException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentResponse;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Unit tests for DefaultHentDokumentService
 *
 * @author Joakim Bj�rnstad, Visma Consulting
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ControllerBeanFactory.class})
public class DefaultHentDokumentServiceTest {

	private static final String SYSTEM_ID = "PENSJON";
	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";

	private static final String BRUKER_ID = "brukerId";
	private static final String CONTENT_TYPE = FilType.RTF.getContentType();
	private static final String STATUS = "status";
	private static final byte[] DOKUMENTDATA = "brevdata".getBytes();

	@Mock
	private ControllerBi controllerMock;

	@InjectMocks
	private DefaultHentDokumentService defaultHentDokumentService;

	@Captor
	private ArgumentCaptor<BrevStatusVO> brevStatusCaptor;

	private HentDokumentRequest request;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		setupControllerMock();
		request = createHentBrevRequest();
	}

	private void setupControllerMock() throws Exception {
		mockStatic(ControllerBeanFactory.class);
		ControllerBeanFactory controllerBeanFactoryMock = mock(ControllerBeanFactory.class);
		when(ControllerBeanFactory.getInstance()).thenReturn(controllerBeanFactoryMock);
		when(controllerBeanFactoryMock.getController()).thenReturn(controllerMock);
	}

	@Test
	public void shouldHentBrev() throws Exception {
		when(controllerMock.hentDokument(brevStatusCaptor.capture())).thenReturn(createBrev());
		when(controllerMock.hentKnappStatus(SYSTEM_ID, BREVREFERANSE)).thenReturn(new KnappStatus(KnappStatus.getDefaultValue()));

		HentDokumentResponse response = defaultHentDokumentService.hentDokument(request);

		BrevStatusVO brevStatus = brevStatusCaptor.getValue();
		assertBrevStatus(brevStatus);
		assertHentBrevResponse(response);
	}

	@Test
	public void shouldThrowExceptionIfBrevWasNotFound() throws Exception {
		thrown.expect(BrevRuntimeException.class);
		thrown.expectMessage("Brevserver fant ikke dokumentet med brevreferanse: " + BREVREFERANSE);

		when(controllerMock.hentDokument(any(BrevStatusVO.class))).thenReturn(null);

		defaultHentDokumentService.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionIfMissingStatus() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("brevStatus must be set");

		defaultHentDokumentService.hentDokument(new HentDokumentRequest());
	}

	private void assertHentBrevResponse(HentDokumentResponse response) {
		assertThat(response.getContentType(), is(CONTENT_TYPE));
		assertThat(response.getDokumentData(), is(DOKUMENTDATA));
		assertThat(response.getKnappStatus(), is(String.valueOf(KnappStatus.getDefaultValue())));
	}

	private void assertBrevStatus(BrevStatusVO brevStatus) {
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getToken(), is(TOKEN));
	}

	private HentDokumentRequest createHentBrevRequest() {
		HentDokumentRequest request = new HentDokumentRequest();
		request.setBrevStatus(createBrevStatus(null));
		return request;
	}

	private BrevStatusVO createBrevStatus(String status) {
		BrevStatusVO brevStatus = new BrevStatusVO();
		brevStatus.setSystemID(SYSTEM_ID);
		brevStatus.setBrevreferanse(BREVREFERANSE);
		brevStatus.setToken(TOKEN);
		brevStatus.setStatus(status);
		return brevStatus;
	}

	private BrevVO createBrev() {
		BrevVO brev = new BrevVO();
		brev.setBrevreferanse(BREVREFERANSE);
		brev.setBrukerID(BRUKER_ID);
		brev.setContentType(CONTENT_TYPE);
		brev.setLagerStatus(STATUS);
		brev.setBrevdata(DOKUMENTDATA);
		brev.setSystemID(SYSTEM_ID);
		return brev;
	}
}
