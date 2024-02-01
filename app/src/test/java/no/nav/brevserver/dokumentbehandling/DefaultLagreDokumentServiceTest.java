package no.nav.brevserver.dokumentbehandling;

import no.nav.brevserver.AbstractBrevserviceTest;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.nais.DokumentbehandlingProvider;
import no.nav.brevserver.nais.support.LagreDokumentRequestMapper;
import no.nav.brevserver.nais.support.impl.DefaultLagreDokumentRequestMapper;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for DefaultLagreDokumentService
 */
@ExtendWith(MockitoExtension.class)
public class DefaultLagreDokumentServiceTest extends AbstractBrevserviceTest {

	@Mock
	private BrevlagerService brevlagerService;
	@Spy
	private LagreDokumentRequestMapper lagreDokumentRequestMapper = new DefaultLagreDokumentRequestMapper();

	@InjectMocks
	private DokumentbehandlingProvider dokumentbehandlingProvider;

	@Captor
	private ArgumentCaptor<BrevVO> brevCaptor;

	@Captor
	private ArgumentCaptor<BrevStatusVO> brevStatusCaptor;

	@Captor
	private ArgumentCaptor<SystemType> systemTypeCaptor;

	@Test
	public void shouldLagreKladdDokumentRtf() throws Exception {
		dokumentbehandlingProvider.lagreDokument(createLagreDokumentRequest(CONTENT_TYPE_RTF));

		verify(brevlagerService).lagreDokument(
				brevCaptor.capture(),
				brevStatusCaptor.capture(),
				systemTypeCaptor.capture()
		);

		assertBrev(brevCaptor.getValue(), CONTENT_TYPE_RTF, Konstanter.BREVLAGER_STATUS_KLADD);
		assertBrevStatus(brevStatusCaptor.getValue(), Konstanter.BREVSTATUS_LAGRET_KLADD);
		assertThat(systemTypeCaptor.getValue(), is(SystemType.PE));
	}

	@Test
	public void shouldThrowExceptionIfValidationFails() throws BrevException {
		var e = assertThrows(NullPointerException.class, () -> dokumentbehandlingProvider.lagreDokument(new LagreDokumentRequest()));

		assertEquals("brevStatus.systemID must be set", e.getMessage());
	}


	@Test
	public void shouldThrowExceptionIfKvitteringskoeMissingAndNewDokumentSet() throws BrevException, IOException {
		LagreDokumentRequest lagreDokumentRequest = createLagreDokumentRequest(CONTENT_TYPE_RTF);
		lagreDokumentRequest.setNyttDokument(true);
		lagreDokumentRequest.setKvitteringskoe(null);

		var e = assertThrows(NullPointerException.class, () -> dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest));

		assertEquals("brevStatus.returKoe must be set if newDocument is true", e.getMessage());
	}

	@Test
	public void shouldThrowExceptionIfMalpakkeMissingAndNewDokumentSet() throws BrevException, IOException {
		LagreDokumentRequest lagreDokumentRequest = createLagreDokumentRequest(CONTENT_TYPE_RTF);
		lagreDokumentRequest.setNyttDokument(true);
		lagreDokumentRequest.setMalpakke(null);

		var e = assertThrows(NullPointerException.class, () -> dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest));

		assertEquals("brevStatus.brevmal must be set if newDocument is true", e.getMessage());
	}

	private void assertBrev(BrevVO brev, String contentType, String lagerStatus) {
		assertThat(brev.getSystemID(), is(SYSTEM_ID));
		assertThat(brev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brev.getContentType(), is(contentType));
		assertThat(brev.getBrevdata(), is(DOKUMENTDATA_RTF));
		assertThat(brev.getBrukerID(), is(BRUKER_ID));
		assertThat(brev.getLagerStatus(), is(lagerStatus));
	}

	private void assertBrevStatus(BrevStatusVO brevStatus, String status) {
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getToken(), is(TOKEN));
		assertThat(brevStatus.getBrevmal(), is(MALPAKKE));
		assertThat(brevStatus.getReturKoe(), is(KVITTERINGSKOE));
		assertThat(brevStatus.getStatus(), is(status));
	}

	private LagreDokumentRequest createLagreDokumentRequest(String contentType) throws IOException {
		LagreDokumentRequest lagreDokumentRequest = new LagreDokumentRequest();
		lagreDokumentRequest.setBrukerId(BRUKER_ID);
		lagreDokumentRequest.setSystemId(SYSTEM_ID);
		lagreDokumentRequest.setBrevreferanse(BREVREFERANSE);
		lagreDokumentRequest.setDokumentData(new DataHandler(new ByteArrayDataSource(DOKUMENTDATA_RTF, FilType.RTF.getContentType())));
		lagreDokumentRequest.setToken(TOKEN);
		lagreDokumentRequest.setMalpakke(MALPAKKE);
		lagreDokumentRequest.setKvitteringskoe(KVITTERINGSKOE);
		return lagreDokumentRequest;
	}


}
