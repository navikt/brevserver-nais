package no.nav.brevserver.support;


import no.nav.brevserver.AbstractBrevserviceTest;
import no.nav.brevserver.app.dokumentbehandling.to.HentDokumentResponse;
import no.nav.brevserver.nais.DokumentbehandlingProvider;
import no.nav.brevserver.nais.support.AvbrytDokumentRequestMapper;
import no.nav.brevserver.nais.support.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.nais.support.HentDokumentRequestMapper;
import no.nav.brevserver.nais.support.HentDokumentResponseMapper;
import no.nav.brevserver.nais.support.LagreDokumentRequestMapper;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
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
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DokumentbehandlingProvider
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(SpringRunner.class)
public class DokumentbehandlingProviderTest extends AbstractBrevserviceTest {

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
	@Mock
	private BrevlagerService brevlagerService;
	@Mock
	private BrevstatusService brevstatusService;

	@InjectMocks
	private DokumentbehandlingProvider dokumentbehandlingProvider;

	@Test
	public void shouldDelegateHentDokumentToDokumentbehandlingService() throws Exception {
		HentDokumentRequest wsRequest = new HentDokumentRequest();
		no.nav.brevserver.app.dokumentbehandling.to.HentDokumentRequest domainRequest =
				new no.nav.brevserver.app.dokumentbehandling.to.HentDokumentRequest();
		HentDokumentResponse domainResponse = new HentDokumentResponse();
		HentDokumentResponse2 wsResponse = new HentDokumentResponse2();
		domainRequest.setBrevStatus(createBrevStatus());
		domainRequest.setBrev(createBrev(CONTENT_TYPE_RTF));
		when(hentDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);
		when(hentDokumentResponseMapper.map(domainResponse)).thenReturn(wsResponse);
		when(brevlagerService.hentDokumentFromBrevlagerOrJoark(domainRequest.getBrevStatus())).thenReturn(new BrevVO());
		when(brevstatusService.hentBrevStatus(any(), any())).thenReturn(createBrevStatus());
		dokumentbehandlingProvider.hentDokument(wsRequest);
		verify(brevlagerService).hentDokumentFromBrevlagerOrJoark(domainRequest.getBrevStatus());
	}

	@Test
	public void shouldDelegateLagreDokumentToDokumentbehandlingService() throws BrevException {
		LagreDokumentRequest wsRequest = new LagreDokumentRequest();
		no.nav.brevserver.app.dokumentbehandling.to.LagreDokumentRequest domainRequest =
				new no.nav.brevserver.app.dokumentbehandling.to.LagreDokumentRequest();
		domainRequest.setBrevStatus(createBrevStatus());
		domainRequest.setBrev(createBrev(CONTENT_TYPE_RTF));
		when(lagreDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);

		dokumentbehandlingProvider.lagreDokument(wsRequest);

		verify(brevlagerService).lagreDokument(domainRequest.getBrev(), domainRequest.getBrevStatus(), SystemType.PE);
	}

	@Test
	public void shouldDelegateAvbrytDokumentToDokumentbehandlingService() throws BrevException {
		AvbrytDokumentRequest wsRequest = new AvbrytDokumentRequest();
		no.nav.brevserver.app.dokumentbehandling.to.AvbrytDokumentRequest domainRequest =
				new no.nav.brevserver.app.dokumentbehandling.to.AvbrytDokumentRequest();
		domainRequest.setBrevStatus(createBrevStatus());
		domainRequest.setBrev(createBrev(CONTENT_TYPE_RTF));
		when(avbrytDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);

		dokumentbehandlingProvider.avbrytDokument(wsRequest);

		verify(brevlagerService).avbrytDokument(domainRequest.getBrevStatus());
	}

	@Test
	public void shouldDelegateFerdigstillDokumentToDokumentbehandlingService() throws BrevException {
		FerdigstillDokumentRequest wsRequest = new FerdigstillDokumentRequest();
		no.nav.brevserver.app.dokumentbehandling.to.FerdigstillDokumentRequest domainRequest =
				new no.nav.brevserver.app.dokumentbehandling.to.FerdigstillDokumentRequest();
		domainRequest.setBrevStatus(createBrevStatus());
		domainRequest.setBrev(createBrev(CONTENT_TYPE_RTF));
		domainRequest.setPdfBrev(createBrev("application/pdf"));
		when(ferdigstillDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);

		dokumentbehandlingProvider.ferdigstillDokument(wsRequest);

		verify(brevlagerService).ferdigstillBrev(domainRequest.getBrevStatus(), domainRequest.getBrev(), domainRequest.getPdfBrev());
	}

	@Test
	public void shouldDelegatePingService() {
		dokumentbehandlingProvider.ping(new PingRequest());

		verify(brevlagerService).ping();
	}


}
