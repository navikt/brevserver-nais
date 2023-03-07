package no.nav.brevserver.dokumentbehandling;

import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.nais.DokumentbehandlingProvider;
import no.nav.brevserver.nais.support.AvbrytDokumentRequestMapper;
import no.nav.brevserver.nais.support.impl.DefaultAvbrytDokumentRequestMapper;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for DefaultAvbrytDokumentService
 */
@RunWith(SpringRunner.class)
public class DefaultAvbrytDokumentServiceTest {
	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";
	private static final String SYSTEM_ID = "PE2";

	@Mock
	private BrevlagerService controllerMock;

	@Spy
	private AvbrytDokumentRequestMapper mapper = new DefaultAvbrytDokumentRequestMapper();

	@InjectMocks
	private DokumentbehandlingProvider dokumentbehandlingProvider;

	@Captor
	private ArgumentCaptor<BrevStatusVO> brevStatusCaptor;

	private AvbrytDokumentRequest request;


	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		request = createAvbrytDokumentRequest();
	}

	@Test
	public void shouldAvbrytDokument() throws Exception {
		dokumentbehandlingProvider.avbrytDokument(request);

		verify(controllerMock).avbrytDokument(brevStatusCaptor.capture());

	}

	@Test
	public void shouldThrowExceptionIfValidationFails() throws BrevException {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("brevStatus.systemID must be set");

		dokumentbehandlingProvider.avbrytDokument(new AvbrytDokumentRequest());
	}

	private AvbrytDokumentRequest createAvbrytDokumentRequest() {
		AvbrytDokumentRequest avbrytDokumentRequest = new AvbrytDokumentRequest();
		avbrytDokumentRequest.setSystemId(SYSTEM_ID);
		avbrytDokumentRequest.setBrevreferanse(BREVREFERANSE);
		avbrytDokumentRequest.setToken(TOKEN);
		return avbrytDokumentRequest;
	}

}
