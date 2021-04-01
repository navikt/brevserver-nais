package no.nav.brevserver.service.dokumentbehandling.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import no.nav.brevserver.controller.ControllerBeanFactory;
import no.nav.brevserver.controller.ControllerBi;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.service.dokumentbehandling.to.AvbrytDokumentRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit tests for DefaultAvbrytDokumentService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ControllerBeanFactory.class})
public class DefaultAvbrytDokumentServiceTest {
	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";
	private static final String SYSTEM_ID = "PE2";

	@Mock
	private ControllerBi controllerMock;

	@InjectMocks
	private DefaultAvbrytDokumentService defaultAvbrytDokumentService;

	private AvbrytDokumentRequest request;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		setupControllerMock();
		request = createAvbrytDokumentRequest();
	}

	private void setupControllerMock() throws Exception {
		mockStatic(ControllerBeanFactory.class);
		ControllerBeanFactory controllerBeanFactoryMock = mock(ControllerBeanFactory.class);
		when(ControllerBeanFactory.getInstance()).thenReturn(controllerBeanFactoryMock);
		when(controllerBeanFactoryMock.getController()).thenReturn(controllerMock);
	}

	@Test
	public void shouldAvbrytDokument() throws Exception {
		defaultAvbrytDokumentService.avbrytDokument(request);

		verify(controllerMock).avbrytDokument(request.getBrevStatus());
	}

	@Test
	public void shouldThrowExceptionIfValidationFails() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("brevStatus must be set");

		defaultAvbrytDokumentService.avbrytDokument(new AvbrytDokumentRequest());
	}

	private AvbrytDokumentRequest createAvbrytDokumentRequest() {
		AvbrytDokumentRequest avbrytDokumentRequest = new AvbrytDokumentRequest();
		avbrytDokumentRequest.setBrevStatus(createBrevStatus());
		return avbrytDokumentRequest;
	}

	private BrevStatusVO createBrevStatus() {
		BrevStatusVO brevStatus = new BrevStatusVO();
		brevStatus.setSystemID(SYSTEM_ID);
		brevStatus.setBrevreferanse(BREVREFERANSE);
		brevStatus.setToken(TOKEN);
		return brevStatus;
	}
}
