package no.nav.brevserver.provider.map.support;

import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import org.junit.Before;
import org.junit.Test;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for DefaultFerdigstillDokumentRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
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
	private no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest domainRequest;

	@Before
	public void setUp() {
		ferdigstillDokumentRequestMapper = new DefaultFerdigstillDokumentRequestMapper();
		wsRequest = createWsFerdigstillDokumentRequest();
	}

	@Test
	public void shouldMapFromWsRequestToDomainRequest() {
		domainRequest = ferdigstillDokumentRequestMapper.map(wsRequest);

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
