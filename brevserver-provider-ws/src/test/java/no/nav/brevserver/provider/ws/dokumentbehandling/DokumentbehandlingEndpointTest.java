package no.nav.brevserver.provider.ws.dokumentbehandling;

import no.nav.brevserver.provider.support.DokumentbehandlingProvider;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for DokumentbehandlingEndpoint
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DokumentbehandlingEndpointTest {

	@Mock
	private DokumentbehandlingProvider dokumentbehandlingProviderMock;

	@InjectMocks
	private DokumentbehandlingEndpoint dokumentbehandlingEndpoint;

	@Test
	public void shoulDelegateToProviderForHentBrev() {
		HentDokumentRequest hentDokumentRequest = new HentDokumentRequest();

		dokumentbehandlingEndpoint.hentDokument(hentDokumentRequest);

		verify(dokumentbehandlingProviderMock).hentDokument(hentDokumentRequest);
	}

	@Test
	public void shoulDelegateToProviderForLagreBrev() {
		LagreDokumentRequest lagreDokumentRequest = new LagreDokumentRequest();

		dokumentbehandlingEndpoint.lagreDokument(lagreDokumentRequest);

		verify(dokumentbehandlingProviderMock).lagreDokument(lagreDokumentRequest);
	}

	@Test
	public void shoulDelegateToProviderForAvbrytBrev() {
		AvbrytDokumentRequest avbrytDokumentRequest = new AvbrytDokumentRequest();

		dokumentbehandlingEndpoint.avbrytDokument(avbrytDokumentRequest);

		verify(dokumentbehandlingProviderMock).avbrytDokument(avbrytDokumentRequest);
	}

	@Test
	public void shoulDelegateToProviderForFerdigstillDokument() {
		FerdigstillDokumentRequest ferdigstillDokumentRequest = new FerdigstillDokumentRequest();

		dokumentbehandlingEndpoint.ferdigstillDokument(ferdigstillDokumentRequest);

		verify(dokumentbehandlingProviderMock).ferdigstillDokument(ferdigstillDokumentRequest);
	}

	@Test
	public void shoulDelegateToProviderForPing() {
		PingRequest pingRequest = new PingRequest();

		dokumentbehandlingEndpoint.ping(pingRequest);

		verify(dokumentbehandlingProviderMock).ping(pingRequest);
	}
}
