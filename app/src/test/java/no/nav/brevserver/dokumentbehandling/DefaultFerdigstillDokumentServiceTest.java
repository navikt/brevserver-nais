package no.nav.brevserver.dokumentbehandling;

import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.nais.DokumentbehandlingProvider;
import no.nav.brevserver.nais.support.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.nais.support.impl.DefaultFerdigstillDokumentRequestMapper;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for DefaultFerdigstillDokumentServiceTest
 */
public class DefaultFerdigstillDokumentServiceTest {

	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";
	private static final String SYSTEM_ID = "PE2";
	private static final String BRUKER_ID = "brukerID";
	private static final String KVITTERINGSKOE = "kvitteringsKoe";
	private static final String MALPAKKE = "malpakke";
	private static final boolean NYTT_DOKUMENT = true;
	private static final String CONTENT_TYPE_PDF = FilType.PDF.getContentType();
	private static final String CONTENT_TYPE_RTF = FilType.RTF.getContentType();
	private static final byte[] DOKUMENTDATA_PDF = "hello pdf".getBytes();
	private static final byte[] DOKUMENTDATA_RTF = "hello rtf".getBytes();

	@Mock
	private BrevlagerService brevlagerService;

	@Spy
	private FerdigstillDokumentRequestMapper ferdigstillDokumentRequestMapper = new DefaultFerdigstillDokumentRequestMapper();
	@InjectMocks
	private DokumentbehandlingProvider dokumentbehandlingProvider;

	@Captor
	private ArgumentCaptor<BrevStatusVO> brevStatusVOCaptor;

	@Captor
	private ArgumentCaptor<BrevVO> brevCaptor;

	@Captor
	private ArgumentCaptor<SystemType> systemTypeCaptor;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldFerdigstilleDokument() throws BrevException {
		dokumentbehandlingProvider.ferdigstillDokument(createFerdigstillDokumentRequest());

		verify(brevlagerService).ferdigstillBrev(brevStatusVOCaptor.capture(),
				brevCaptor.capture(), brevCaptor.capture());

		assertBrevStatus(brevStatusVOCaptor.getValue());
		assertRtfBrev(brevCaptor.getAllValues().get(0));
		assertPdfBrev(brevCaptor.getAllValues().get(1));
	}

	@Test
	public void shouldThrowExceptionIfValidationFails() throws BrevException {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("brevStatus.systemID must be set");

		dokumentbehandlingProvider.ferdigstillDokument(new FerdigstillDokumentRequest());
	}

	@Test
	public void shouldThrowExceptionIfKvitteringskoeMissingAndNewDokumentSet() throws BrevException {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("brevStatus.returKoe must be set if newDocument is true");

		FerdigstillDokumentRequest ferdigstillDokumentRequest = createFerdigstillDokumentRequest();
		ferdigstillDokumentRequest.setNyttDokument(true);
		ferdigstillDokumentRequest.setKvitteringskoe(null);

		dokumentbehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);
	}

	@Test
	public void shouldThrowExceptionIfMalpakkeMissingAndNewDokumentSet() throws BrevException {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("brevStatus.brevmal must be set if newDocument is true");

		FerdigstillDokumentRequest ferdigstillDokumentRequest = createFerdigstillDokumentRequest();
		ferdigstillDokumentRequest.setNyttDokument(true);
		ferdigstillDokumentRequest.setMalpakke(null);

		dokumentbehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);
	}

	private void assertBrevStatus(BrevStatusVO brevStatus) {
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getToken(), is(TOKEN));
		assertThat(brevStatus.getReturKoe(), is(KVITTERINGSKOE));
		assertThat(brevStatus.getBrevmal(), is(MALPAKKE));
	}

	private void assertRtfBrev(BrevVO brev) {
		assertThat(brev.getBrevdata(), is(DOKUMENTDATA_RTF));
		assertThat(brev.getContentType(), is(CONTENT_TYPE_RTF));
		assertBrev(brev);
	}

	private void assertPdfBrev(BrevVO brev) {
		assertThat(brev.getBrevdata(), is(DOKUMENTDATA_PDF));
		assertThat(brev.getContentType(), is(CONTENT_TYPE_PDF));
		assertBrev(brev);
	}

	private void assertBrev(BrevVO brev) {
		assertThat(brev.getBrukerID(), is(BRUKER_ID));
		assertThat(brev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brev.getSystemID(), is(SYSTEM_ID));
	}

	private FerdigstillDokumentRequest createFerdigstillDokumentRequest() {
		FerdigstillDokumentRequest ferdigstillDokumentRequest = new FerdigstillDokumentRequest();
		ferdigstillDokumentRequest.setSystemId(SYSTEM_ID);
		ferdigstillDokumentRequest.setBrevreferanse(BREVREFERANSE);
		ferdigstillDokumentRequest.setToken(TOKEN);
		ferdigstillDokumentRequest.setMalpakke(MALPAKKE);
		ferdigstillDokumentRequest.setKvitteringskoe(KVITTERINGSKOE);
		ferdigstillDokumentRequest.setBrukerId(BRUKER_ID);
		ferdigstillDokumentRequest.setPdfDokument(new DataHandler(new ByteArrayDataSource(DOKUMENTDATA_PDF, "application/pdf")));
		ferdigstillDokumentRequest.setRedDokument(new DataHandler(new ByteArrayDataSource(DOKUMENTDATA_RTF, "text/rtf")));
		ferdigstillDokumentRequest.setNyttDokument(NYTT_DOKUMENT);
		return ferdigstillDokumentRequest;
	}

}
