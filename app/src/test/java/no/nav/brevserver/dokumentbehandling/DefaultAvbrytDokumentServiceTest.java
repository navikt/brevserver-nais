package no.nav.brevserver.dokumentbehandling;

import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.nais.DokumentbehandlingProvider;
import no.nav.brevserver.nais.support.AvbrytDokumentRequestMapper;
import no.nav.brevserver.nais.support.impl.DefaultAvbrytDokumentRequestMapper;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
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

	@Test
	public void shouldAvbrytDokument() throws Exception {
		dokumentbehandlingProvider.avbrytDokument(createAvbrytDokumentRequest());

		verify(controllerMock).avbrytDokument(brevStatusCaptor.capture());
	}

	@Test
	public void shouldThrowExceptionIfValidationFails() {
		assertThatNullPointerException()
				.isThrownBy(() -> dokumentbehandlingProvider.avbrytDokument(new AvbrytDokumentRequest()))
				.withMessage("brevStatus.systemID must be set");
	}

	private AvbrytDokumentRequest createAvbrytDokumentRequest() {
		AvbrytDokumentRequest avbrytDokumentRequest = new AvbrytDokumentRequest();
		avbrytDokumentRequest.setSystemId(SYSTEM_ID);
		avbrytDokumentRequest.setBrevreferanse(BREVREFERANSE);
		avbrytDokumentRequest.setToken(TOKEN);
		return avbrytDokumentRequest;
	}

}