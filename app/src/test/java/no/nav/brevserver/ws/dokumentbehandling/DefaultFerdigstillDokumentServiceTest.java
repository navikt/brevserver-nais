package no.nav.brevserver.ws.dokumentbehandling;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.ws.dokumentbehandling.support.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.ws.dokumentbehandling.support.impl.DefaultFerdigstillDokumentRequestMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;

import static no.nav.brevserver.core.vo.FilType.PDF;
import static no.nav.brevserver.core.vo.FilType.RTF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DefaultFerdigstillDokumentServiceTest {

	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";
	private static final String SYSTEM_ID = "PE2";
	private static final String BRUKER_ID = "brukerID";
	private static final String KVITTERINGSKOE = "kvitteringsKoe";
	private static final String MALPAKKE = "malpakke";
	private static final boolean NYTT_DOKUMENT = true;
	private static final String CONTENT_TYPE_PDF = PDF.getContentType();
	private static final String CONTENT_TYPE_RTF = RTF.getContentType();
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

	@Test
	public void shouldFerdigstilleDokument() throws BrevException {
		dokumentbehandlingProvider.ferdigstillDokument(createFerdigstillDokumentRequest());

		verify(brevlagerService).ferdigstillBrev(brevStatusVOCaptor.capture(),
				brevCaptor.capture(), brevCaptor.capture());

		var brevstatus = brevStatusVOCaptor.getValue();
		assertThat(brevstatus.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(brevstatus.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(brevstatus.getToken()).isEqualTo(TOKEN);
		assertThat(brevstatus.getReturKoe()).isEqualTo(KVITTERINGSKOE);
		assertThat(brevstatus.getBrevmal()).isEqualTo(MALPAKKE);

		var rtfBrev = brevCaptor.getAllValues().getFirst();
		assertThat(rtfBrev.getBrevdata()).isEqualTo(DOKUMENTDATA_RTF);
		assertThat(rtfBrev.getContentType()).isEqualTo(CONTENT_TYPE_RTF);
		assertThat(rtfBrev.getBrukerID()).isEqualTo(BRUKER_ID);
		assertThat(rtfBrev.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(rtfBrev.getSystemID()).isEqualTo(SYSTEM_ID);

		var pdfBrev = brevCaptor.getAllValues().get(1);
		assertThat(pdfBrev.getBrevdata()).isEqualTo(DOKUMENTDATA_PDF);
		assertThat(pdfBrev.getContentType()).isEqualTo(CONTENT_TYPE_PDF);
		assertThat(pdfBrev.getBrukerID()).isEqualTo(BRUKER_ID);
		assertThat(pdfBrev.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(pdfBrev.getSystemID()).isEqualTo(SYSTEM_ID);
	}

	@Test
	public void shouldFailIfBrevdataIsEmpty() throws IOException {
		var request = createFerdigstillDokumentRequest();
		request.setPdfDokument(new DataHandler(new ByteArrayDataSource(InputStream.nullInputStream(), "application/pdf")));

		assertThatIllegalArgumentException()
				.isThrownBy(() -> dokumentbehandlingProvider.ferdigstillDokument(request))
				.withMessage("Dokumentet kan ikke ferdigstilles da PDF-dokumentet er tomt");
	}

	@Test
	public void shouldThrowExceptionIfValidationFails() {
		assertThatNullPointerException()
				.isThrownBy(() -> dokumentbehandlingProvider.ferdigstillDokument(new FerdigstillDokumentRequest()))
				.withMessage("brevStatus.systemID must be set");
	}

	@Test
	public void shouldThrowExceptionIfKvitteringskoeMissingAndNewDokumentSet() {
		FerdigstillDokumentRequest ferdigstillDokumentRequest = createFerdigstillDokumentRequest();
		ferdigstillDokumentRequest.setNyttDokument(true);
		ferdigstillDokumentRequest.setKvitteringskoe(null);

		assertThatNullPointerException()
				.isThrownBy(() -> dokumentbehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest))
				.withMessage("brevStatus.returKoe must be set if newDocument is true");
	}

	@Test
	public void shouldThrowExceptionIfMalpakkeMissingAndNewDokumentSet() {
		FerdigstillDokumentRequest ferdigstillDokumentRequest = createFerdigstillDokumentRequest();
		ferdigstillDokumentRequest.setNyttDokument(true);
		ferdigstillDokumentRequest.setMalpakke(null);

		assertThatNullPointerException()
				.isThrownBy(() -> dokumentbehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest))
				.withMessage("brevStatus.brevmal must be set if newDocument is true");
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