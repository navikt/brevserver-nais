package no.nav.brevserver.support;

import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.nais.support.impl.DefaultFerdigstillDokumentRequestMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for DefaultFerdigstillDokumentRequestMapper
 */
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

	private DefaultFerdigstillDokumentRequestMapper ferdigstillDokumentRequestMapper;
	private FerdigstillDokumentRequest wsRequest;
	private no.nav.brevserver.app.dokumentbehandling.to.FerdigstillDokumentRequest domainRequest;

	@BeforeEach
	public void setUp() {
		ferdigstillDokumentRequestMapper = new DefaultFerdigstillDokumentRequestMapper();
		wsRequest = createWsFerdigstillDokumentRequest();
	}

	@Test
	public void shouldMapFromWsRequestToDomainRequest() {
		domainRequest = ferdigstillDokumentRequestMapper.map(wsRequest);

		assertThat(domainRequest.isNewDocument(), is(NYTT_DOKUMENT));
		assertBrevStatus(domainRequest.getBrevStatus());
		assertRtfBrev(domainRequest.getBrev());
		assertPdfBrev(domainRequest.getPdfBrev());
	}

	private void assertBrevStatus(BrevStatusVO brevStatus) {
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getToken(), is(TOKEN));
		assertThat(brevStatus.getBrevmal(), is(MALPAKKE));
		assertThat(brevStatus.getReturKoe(), is(KVITTERINGSKOE));
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
