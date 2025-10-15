package no.nav.brevserver.support;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.nais.support.impl.DefaultFerdigstillDokumentRequestMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultFerdigstillDokumentRequestMapperTest {

	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";
	private static final String SYSTEM_ID = "PE2";
	private static final String BRUKER_ID = "brukerID";
	private static final String KVITTERINGSKOE = "kvitteringsKoe";
	private static final String MALPAKKE = "malpakke";
	private static final boolean NYTT_DOKUMENT = true;
	private static final byte[] DOKUMENTDATA_RTF = "hello rtf".getBytes();
	private static final String CONTENT_TYPE_RTF = FilType.RTF.getContentType();
	private static final byte[] DOKUMENTDATA_PDF = "hello pdf".getBytes();
	private static final String CONTENT_TYPE_PDF = FilType.PDF.getContentType();

	private final DefaultFerdigstillDokumentRequestMapper ferdigstillDokumentRequestMapper = new DefaultFerdigstillDokumentRequestMapper();

	@Test
	public void shouldMapFromWsRequestToDomainRequest() {
		no.nav.brevserver.app.dokumentbehandling.to.FerdigstillDokumentRequest domainRequest = ferdigstillDokumentRequestMapper.map(createWsFerdigstillDokumentRequest());

		assertThat(domainRequest.isNewDocument()).isEqualTo(NYTT_DOKUMENT);

		var brevstatus = domainRequest.getBrevStatus();
		assertThat(brevstatus.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(brevstatus.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(brevstatus.getToken()).isEqualTo(TOKEN);
		assertThat(brevstatus.getBrevmal()).isEqualTo(MALPAKKE);
		assertThat(brevstatus.getReturKoe()).isEqualTo(KVITTERINGSKOE);

		var rtfBrev = domainRequest.getBrev();
		assertThat(rtfBrev.getBrevdata()).isEqualTo(DOKUMENTDATA_RTF);
		assertThat(rtfBrev.getContentType()).isEqualTo(CONTENT_TYPE_RTF);
		assertThat(rtfBrev.getBrukerID()).isEqualTo(BRUKER_ID);
		assertThat(rtfBrev.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(rtfBrev.getSystemID()).isEqualTo(SYSTEM_ID);

		var pdfBrev = domainRequest.getPdfBrev();
		assertThat(pdfBrev.getBrevdata()).isEqualTo(DOKUMENTDATA_PDF);
		assertThat(pdfBrev.getContentType()).isEqualTo(CONTENT_TYPE_PDF);
		assertThat(pdfBrev.getBrukerID()).isEqualTo(BRUKER_ID);
		assertThat(pdfBrev.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(pdfBrev.getSystemID()).isEqualTo(SYSTEM_ID);
	}

	private FerdigstillDokumentRequest createWsFerdigstillDokumentRequest() {
		FerdigstillDokumentRequest ferdigstillDokumentRequest = new FerdigstillDokumentRequest();
		ferdigstillDokumentRequest.setBrevreferanse(BREVREFERANSE);
		ferdigstillDokumentRequest.setToken(TOKEN);
		ferdigstillDokumentRequest.setSystemId(SYSTEM_ID);
		ferdigstillDokumentRequest.setBrukerId(BRUKER_ID);
		ferdigstillDokumentRequest.setKvitteringskoe(KVITTERINGSKOE);
		ferdigstillDokumentRequest.setMalpakke(MALPAKKE);
		ferdigstillDokumentRequest.setNyttDokument(NYTT_DOKUMENT);
		ferdigstillDokumentRequest.setRedDokument(new DataHandler(new ByteArrayDataSource(DOKUMENTDATA_RTF, CONTENT_TYPE_RTF)));
		ferdigstillDokumentRequest.setPdfDokument(new DataHandler(new ByteArrayDataSource(DOKUMENTDATA_PDF, CONTENT_TYPE_PDF)));
		return ferdigstillDokumentRequest;
	}

}