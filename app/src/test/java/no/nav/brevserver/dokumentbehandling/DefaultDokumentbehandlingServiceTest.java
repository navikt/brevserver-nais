package no.nav.brevserver.dokumentbehandling;

import no.nav.brevserver.nais.DokumentbehandlingProvider;
import no.nav.brevserver.nais.support.AvbrytDokumentRequestMapper;
import no.nav.brevserver.nais.support.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.nais.support.HentDokumentRequestMapper;
import no.nav.brevserver.nais.support.HentDokumentResponseMapper;
import no.nav.brevserver.nais.support.LagreDokumentRequestMapper;
import no.nav.brevserver.nais.support.impl.DefaultAvbrytDokumentRequestMapper;
import no.nav.brevserver.nais.support.impl.DefaultFerdigstillDokumentRequestMapper;
import no.nav.brevserver.nais.support.impl.DefaultHentDokumentRequestMapper;
import no.nav.brevserver.nais.support.impl.DefaultHentDokumentResponseMapper;
import no.nav.brevserver.nais.support.impl.DefaultLagreDokumentRequestMapper;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DefaultDokumentbehandlingServiceTest
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(SpringRunner.class)
public class DefaultDokumentbehandlingServiceTest {

	private static final String SYSTEM_ID = "PENSJON";
	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";

	private static final String BRUKER_ID = "brukerId";
	private static final String CONTENT_TYPE = FilType.RTF.getContentType();
	private static final String STATUS = "status";
	private static final byte[] DOKUMENTDATA = "brevdata".getBytes();
	private static final String KVITTERINGSKOE = "kvitteringsKoe";
	private static final String MALPAKKE = "malpakke";

	@Mock
	private BrevlagerService brevlagerService;
	@Mock
	private BrevstatusService brevstatusService;

	@InjectMocks
	private DokumentbehandlingProvider dokumentbehandlingProvider;

	@Mock
	private LagreDokumentRequestMapper lagreDokumentRequestMapper;
	@Mock
	private AvbrytDokumentRequestMapper avbrytDokumentRequestMapper;
	@Mock
	private FerdigstillDokumentRequestMapper ferdigstillDokumentRequestMapper;
	@Mock
	private HentDokumentRequestMapper hentDokumentRequestMapper;
	@Spy
	private HentDokumentResponseMapper hentDokumentResponseMapper = new DefaultHentDokumentResponseMapper();

	@Captor
	private ArgumentCaptor<BrevStatusVO> brevStatusCaptor;
	@Captor
	private ArgumentCaptor<BrevVO> brevCaptor;
	@Captor
	private ArgumentCaptor<SystemType> systemTypeCaptor;

	@Test
	public void shouldCallHentDokument() throws BrevFunctionalException, BrevTechnicalException {
		HentDokumentRequest hentDokumentRequest = createHentDokumentRequest();
		when(brevlagerService.hentDokumentFromBrevlagerOrJoark(any())).thenReturn(createBrev());
		no.nav.brevserver.app.dokumentbehandling.to.HentDokumentRequest request = mock(no.nav.brevserver.app.dokumentbehandling.to.HentDokumentRequest.class);
		when(request.getBrevStatus()).thenReturn(createBrevStatus());
		when(hentDokumentRequestMapper.map(any())).thenReturn(request);
		dokumentbehandlingProvider.hentDokument(hentDokumentRequest);
		verify(brevlagerService).hentDokumentFromBrevlagerOrJoark(brevStatusCaptor.capture());
		BrevStatusVO brevStatusVOCapt = brevStatusCaptor.getValue();
		assertEquals(brevStatusVOCapt.getSystemID(), hentDokumentRequest.getSystemId());
		assertEquals(brevStatusVOCapt.getBrevreferanse(), hentDokumentRequest.getBrevreferanse());
	}


	@Test
	public void shouldCallLagreDokument() throws BrevException {
		LagreDokumentRequest lagreDokumentRequest = new LagreDokumentRequest();
		no.nav.brevserver.app.dokumentbehandling.to.LagreDokumentRequest request = mock(no.nav.brevserver.app.dokumentbehandling.to.LagreDokumentRequest.class);
		when(request.getBrevStatus()).thenReturn(createBrevStatus());
		when(request.getBrev()).thenReturn(createBrev());
		when(lagreDokumentRequestMapper.map(any())).thenReturn(request);
		dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest);

		verify(brevlagerService).lagreDokument(brevCaptor.capture(), brevStatusCaptor.capture(), systemTypeCaptor.capture());
		BrevStatusVO brevStatusVO = brevStatusCaptor.getValue();
		assertEquals(brevStatusVO.getSystemID(), SYSTEM_ID);
		assertEquals(brevStatusVO.getBrevreferanse(), BREVREFERANSE);
		assertEquals(systemTypeCaptor.getValue(), SystemType.PE);
	}

	@Test
	public void shouldCallAvbrytDokument() throws BrevException {
		AvbrytDokumentRequest avbrytDokumentRequest = new AvbrytDokumentRequest();
		no.nav.brevserver.app.dokumentbehandling.to.AvbrytDokumentRequest request = mock(no.nav.brevserver.app.dokumentbehandling.to.AvbrytDokumentRequest.class);
		when(request.getBrevStatus()).thenReturn(createBrevStatus());
		when(avbrytDokumentRequestMapper.map(any())).thenReturn(request);
		dokumentbehandlingProvider.avbrytDokument(avbrytDokumentRequest);

		verify(brevlagerService).avbrytDokument(brevStatusCaptor.capture());
		BrevStatusVO brevStatusVO = brevStatusCaptor.getValue();
		assertEquals(brevStatusVO.getSystemID(), SYSTEM_ID);
		assertEquals(brevStatusVO.getBrevreferanse(), BREVREFERANSE);
	}

	@Test
	public void shouldCallFerdigstillDokument() throws BrevException {
		FerdigstillDokumentRequest ferdigstillDokumentRequest = new FerdigstillDokumentRequest();
		no.nav.brevserver.app.dokumentbehandling.to.FerdigstillDokumentRequest request = mock(no.nav.brevserver.app.dokumentbehandling.to.FerdigstillDokumentRequest.class);
		when(request.getBrevStatus()).thenReturn(createBrevStatus());
		when(ferdigstillDokumentRequestMapper.map(any())).thenReturn(request);
		dokumentbehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);

		verify(brevlagerService).ferdigstillBrev(brevStatusCaptor.capture(), any(), any());
		BrevStatusVO brevStatusVO = brevStatusCaptor.getValue();
		assertEquals(brevStatusVO.getSystemID(), SYSTEM_ID);
		assertEquals(brevStatusVO.getBrevreferanse(), BREVREFERANSE);
	}

	private HentDokumentRequest createHentDokumentRequest() {
		HentDokumentRequest request = new HentDokumentRequest();
		request.setSystemId(SYSTEM_ID);
		request.setBrevreferanse(BREVREFERANSE);
		request.setToken(TOKEN);
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
		brevStatus.setBrevmal(MALPAKKE);
		brevStatus.setReturKoe(KVITTERINGSKOE);
		return brevStatus;
	}

}
