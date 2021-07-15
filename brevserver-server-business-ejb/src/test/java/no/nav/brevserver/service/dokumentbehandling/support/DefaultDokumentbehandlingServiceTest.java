package no.nav.brevserver.service.dokumentbehandling.support;

import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.service.dokumentbehandling.AvbrytDokumentService;
import no.nav.brevserver.service.dokumentbehandling.FerdigstillDokumentService;
import no.nav.brevserver.service.dokumentbehandling.HentDokumentService;
import no.nav.brevserver.service.dokumentbehandling.LagreDokumentService;
import no.nav.brevserver.service.dokumentbehandling.to.AvbrytDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentRequest;
import no.nav.brevserver.service.dokumentbehandling.to.LagreDokumentRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for DefaultDokumentbehandlingServiceTest
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultDokumentbehandlingServiceTest {
	@Mock
	private HentDokumentService hentDokumentServiceMock;
	@Mock
	private LagreDokumentService lagreDokumentServiceMock;
	@Mock
	private AvbrytDokumentService avbrytDokumentServiceMock;
	@Mock
	private FerdigstillDokumentService ferdigstillDokumentServiceMock;

	@InjectMocks
	private DefaultDokumentbehandlingService defaultDokumentbehandlingService;

	@Test
	public void shouldCallHentDokument() throws BrevFunctionalException {
		HentDokumentRequest hentDokumentRequest = new HentDokumentRequest();

		defaultDokumentbehandlingService.hentDokument(hentDokumentRequest);

		verify(hentDokumentServiceMock).hentDokument(hentDokumentRequest);
	}

	@Test
	public void shouldCallLagreDokument() {
		LagreDokumentRequest lagreDokumentRequest = new LagreDokumentRequest();

		defaultDokumentbehandlingService.lagreDokument(lagreDokumentRequest);

		verify(lagreDokumentServiceMock).lagreDokument(lagreDokumentRequest);
	}

	@Test
	public void shouldCallAvbrytDokument() {
		AvbrytDokumentRequest avbrytDokumentRequest = new AvbrytDokumentRequest();

		defaultDokumentbehandlingService.avbrytDokument(avbrytDokumentRequest);

		verify(avbrytDokumentServiceMock).avbrytDokument(avbrytDokumentRequest);
	}

	@Test
	public void shouldCallFerdigstillDokument() {
		FerdigstillDokumentRequest ferdigstillDokumentRequest = new FerdigstillDokumentRequest();

		defaultDokumentbehandlingService.ferdigstillDokument(ferdigstillDokumentRequest);

		verify(ferdigstillDokumentServiceMock).ferdigstillDokument(ferdigstillDokumentRequest);
	}
}
