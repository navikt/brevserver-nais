package no.nav.brevserver.provider.support;

import no.nav.brevserver.provider.map.AvbrytDokumentRequestMapper;
import no.nav.brevserver.provider.map.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.provider.map.HentDokumentRequestMapper;
import no.nav.brevserver.provider.map.HentDokumentResponseMapper;
import no.nav.brevserver.provider.map.LagreDokumentRequestMapper;
import no.nav.brevserver.service.dokumentbehandling.DokumentbehandlingService;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentResponse;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DokumentbehandlingProvider
 *
 * @author Joakim Bj�rnstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DokumentbehandlingProviderTest {

	@Mock
	private DokumentbehandlingService dokumentbehandlingService;
	@Mock
	private HentDokumentRequestMapper hentDokumentRequestMapper;
	@Mock
	private HentDokumentResponseMapper hentDokumentResponseMapper;
	@Mock
	private LagreDokumentRequestMapper lagreDokumentRequestMapper;
	@Mock
	private AvbrytDokumentRequestMapper avbrytDokumentRequestMapper;
	@Mock
	private FerdigstillDokumentRequestMapper ferdigstillDokumentRequestMapper;

	@InjectMocks
	private DokumentbehandlingProvider dokumentbehandlingProvider;

	@Test
	public void shouldDelegateHentDokumentToDokumentbehandlingService() throws Exception {
		HentDokumentRequest wsRequest = new HentDokumentRequest();
		no.nav.brevserver.service.dokumentbehandling.to.HentDokumentRequest domainRequest =
				new no.nav.brevserver.service.dokumentbehandling.to.HentDokumentRequest();
		when(hentDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);

		dokumentbehandlingProvider.hentDokument(wsRequest);

		verify(dokumentbehandlingService).hentDokument(domainRequest);
	}

	@Test
	public void shouldDelegateLagreDokumentToDokumentbehandlingService() {
		LagreDokumentRequest wsRequest = new LagreDokumentRequest();
		no.nav.brevserver.service.dokumentbehandling.to.LagreDokumentRequest domainRequest =
				new no.nav.brevserver.service.dokumentbehandling.to.LagreDokumentRequest();

		when(lagreDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);

		dokumentbehandlingProvider.lagreDokument(wsRequest);

		verify(dokumentbehandlingService).lagreDokument(domainRequest);
	}

	@Test
	public void shouldDelegateAvbrytDokumentToDokumentbehandlingService() {
		AvbrytDokumentRequest wsRequest = new AvbrytDokumentRequest();
		no.nav.brevserver.service.dokumentbehandling.to.AvbrytDokumentRequest domainRequest =
				new no.nav.brevserver.service.dokumentbehandling.to.AvbrytDokumentRequest();

		when(avbrytDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);

		dokumentbehandlingProvider.avbrytDokument(wsRequest);

		verify(dokumentbehandlingService).avbrytDokument(domainRequest);
	}

	@Test
	public void shouldDelegateFerdigstillDokumentToDokumentbehandlingService() {
		FerdigstillDokumentRequest wsRequest = new FerdigstillDokumentRequest();
		no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest domainRequest =
				new no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest();

		when(ferdigstillDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);

		dokumentbehandlingProvider.ferdigstillDokument(wsRequest);

		verify(dokumentbehandlingService).ferdigstillDokument(domainRequest);
	}

	@Test
	public void shouldDelegatePingService() {
		dokumentbehandlingProvider.ping(new PingRequest());

		verify(dokumentbehandlingService).ping();
	}
}
