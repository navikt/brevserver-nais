package no.nav.brevserver.dokumentbehandling;

import no.nav.brevserver.core.constants.KnappStatus;
import no.nav.brevserver.core.exception.BrevFinnesIkkeException;
import no.nav.brevserver.core.exception.BrevRuntimeException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.nais.DokumentbehandlingProvider;
import no.nav.brevserver.nais.support.HentDokumentRequestMapper;
import no.nav.brevserver.nais.support.HentDokumentResponseMapper;
import no.nav.brevserver.nais.support.impl.DefaultHentDokumentRequestMapper;
import no.nav.brevserver.nais.support.impl.DefaultHentDokumentResponseMapper;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
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

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DefaultHentDokumentService
 *
 * @author Joakim BjØrnstad, Visma Consulting
 */
@RunWith(SpringRunner.class)
public class DefaultHentDokumentServiceTest {

	private static final String SYSTEM_ID = "PENSJON";
	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";

	private static final String BRUKER_ID = "brukerId";
	private static final String CONTENT_TYPE = FilType.RTF.getContentType();
	private static final String STATUS = "status";
	private static final byte[] DOKUMENTDATA = "brevdata".getBytes();

	@Mock
	private BrevlagerService brevlagerService;
	@Spy
	private HentDokumentRequestMapper mapper = new DefaultHentDokumentRequestMapper();
	@Mock
	private BrevstatusService brevstatusService;
	@InjectMocks
	private DokumentbehandlingProvider dokumentbehandlingProvider;
	@Spy
	private HentDokumentResponseMapper hentDokumentResponseMapper = new DefaultHentDokumentResponseMapper();

	@Captor
	private ArgumentCaptor<BrevStatusVO> brevStatusCaptor;

	private HentDokumentRequest request;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		request = createHentBrevRequest();
	}

	@Test
	public void shouldHentBrev() throws Exception {
		when(brevlagerService.hentDokumentFromBrevlagerOrJoark(any())).thenReturn(createBrev());
		when(brevstatusService.hentBrevStatus(any(), any())).thenReturn(createBrevStatus());
		HentDokumentResponse2 response = dokumentbehandlingProvider.hentDokument(request);
		verify(brevlagerService).hentDokumentFromBrevlagerOrJoark(brevStatusCaptor.capture());
		BrevStatusVO brevStatus = brevStatusCaptor.getValue();
		assertBrevStatus(brevStatus);
		assertHentBrevResponse(response);
	}

	@Test
	public void shouldThrowExceptionIfBrevWasNotFound() throws Exception {
		thrown.expect(BrevFinnesIkkeException.class);
		thrown.expectMessage("Brevserver fant ikke dokumentet med brevreferanse: " + BREVREFERANSE);

		when(brevlagerService.hentDokumentFromBrevlagerOrJoark(any(BrevStatusVO.class))).thenReturn(null);

		dokumentbehandlingProvider.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionIfMissingStatus() throws Exception {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("brevStatus.systemID must be set");

		dokumentbehandlingProvider.hentDokument(new HentDokumentRequest());
	}

	private void assertHentBrevResponse(HentDokumentResponse2 response) throws IOException {
		assertThat(response.getDokumentData().getContentType(), is(CONTENT_TYPE));
		assertThat(response.getDokumentData().getInputStream().readAllBytes(), is(DOKUMENTDATA));
		assertThat(response.getKnappStatus(), is(String.valueOf(KnappStatus.getDefaultValue())));
	}

	private void assertBrevStatus(BrevStatusVO brevStatus) {
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getToken(), is(TOKEN));
	}

	private HentDokumentRequest createHentBrevRequest() {
		HentDokumentRequest request = new HentDokumentRequest();
		request.setToken(TOKEN);
		request.setBrevreferanse(BREVREFERANSE);
		request.setSystemId(SYSTEM_ID);
		return request;
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

	private BrevStatusVO createBrevStatus() {
		BrevStatusVO brevStatus = new BrevStatusVO();
		brevStatus.setSystemID(SYSTEM_ID);
		brevStatus.setBrevreferanse(BREVREFERANSE);
		brevStatus.setToken(TOKEN);
		return brevStatus;
	}
}
