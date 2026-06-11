package no.nav.brevserver.ws.dokumentbehandling;

import no.nav.brevserver.AbstractBrevserviceTest;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.ws.dokumentbehandling.support.AvbrytDokumentRequestMapper;
import no.nav.brevserver.ws.dokumentbehandling.support.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.ws.dokumentbehandling.support.HentDokumentRequestMapper;
import no.nav.brevserver.ws.dokumentbehandling.support.HentDokumentResponseMapper;
import no.nav.brevserver.ws.dokumentbehandling.support.LagreDokumentRequestMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.brevserver.core.constants.SystemType.PE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
		no.nav.brevserver.ws.dokumentbehandling.to.HentDokumentRequest domainRequest =
				new no.nav.brevserver.ws.dokumentbehandling.to.HentDokumentRequest();
		domainRequest.setBrevStatus(createBrevStatus());
		domainRequest.setBrev(createBrev(CONTENT_TYPE_RTF));
		when(hentDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);
		when(brevlagerService.hentDokumentFromBrevlagerOrJoark(domainRequest.getBrevStatus())).thenReturn(new BrevVO());
		when(brevstatusService.hentBrevStatus(any(), any())).thenReturn(createBrevStatus());

		dokumentbehandlingProvider.hentDokument(wsRequest);

		verify(brevlagerService).hentDokumentFromBrevlagerOrJoark(domainRequest.getBrevStatus());
	}

	@Test
	public void shouldDelegateLagreDokumentToDokumentbehandlingService() throws BrevException {
		LagreDokumentRequest wsRequest = new LagreDokumentRequest();
		no.nav.brevserver.ws.dokumentbehandling.to.LagreDokumentRequest domainRequest =
				new no.nav.brevserver.ws.dokumentbehandling.to.LagreDokumentRequest();
		domainRequest.setBrevStatus(createBrevStatus());
		domainRequest.setBrev(createBrev(CONTENT_TYPE_RTF));
		when(lagreDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);

		dokumentbehandlingProvider.lagreDokument(wsRequest);

		verify(brevlagerService).lagreDokument(domainRequest.getBrev(), domainRequest.getBrevStatus(), PE);
	}

	@Test
	public void shouldDelegateAvbrytDokumentToDokumentbehandlingService() throws BrevException {
		AvbrytDokumentRequest wsRequest = new AvbrytDokumentRequest();
		no.nav.brevserver.ws.dokumentbehandling.to.AvbrytDokumentRequest domainRequest =
				new no.nav.brevserver.ws.dokumentbehandling.to.AvbrytDokumentRequest();
		domainRequest.setBrevStatus(createBrevStatus());
		domainRequest.setBrev(createBrev(CONTENT_TYPE_RTF));
		when(avbrytDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);

		dokumentbehandlingProvider.avbrytDokument(wsRequest);

		verify(brevlagerService).avbrytDokument(domainRequest.getBrevStatus());
	}

	@Test
	public void shouldDelegateFerdigstillDokumentToDokumentbehandlingService() throws BrevException {
		FerdigstillDokumentRequest wsRequest = new FerdigstillDokumentRequest();
		no.nav.brevserver.ws.dokumentbehandling.to.FerdigstillDokumentRequest domainRequest =
				new no.nav.brevserver.ws.dokumentbehandling.to.FerdigstillDokumentRequest();
		domainRequest.setBrevStatus(createBrevStatus());
		domainRequest.setBrev(createBrev(CONTENT_TYPE_RTF));
		domainRequest.setPdfBrev(createBrev("application/pdf"));
		when(ferdigstillDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);

		dokumentbehandlingProvider.ferdigstillDokument(wsRequest);

		verify(brevlagerService).ferdigstillBrev(domainRequest.getBrevStatus(), domainRequest.getBrev(), domainRequest.getPdfBrev());
	}

}