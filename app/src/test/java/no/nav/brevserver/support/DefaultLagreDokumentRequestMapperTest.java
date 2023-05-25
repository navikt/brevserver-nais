package no.nav.brevserver.support;

import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.nais.support.impl.DefaultLagreDokumentRequestMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for DefaultLagreDokumentRequestMapper
 */
public class DefaultLagreDokumentRequestMapperTest {
	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";
	private static final String SYSTEM_ID = "PE2";
	private static final String BRUKER_ID = "brukerID";
	private static final String KVITTERINGSKOE = "kvitteringsKoe";
	private static final String MALPAKKE = "malpakke";
	private static final boolean NYTT_DOKUMENT = true;
	private static final byte[] DOKUMENTDATA = "hello world".getBytes();
	private static final String CONTENT_TYPE = FilType.RTF.getContentType();

	private DefaultLagreDokumentRequestMapper lagreBrevRequestMapper;
	private LagreDokumentRequest wsRequest;
	private no.nav.brevserver.app.dokumentbehandling.to.LagreDokumentRequest domainRequest;

	@BeforeEach
	public void setUp() {
		lagreBrevRequestMapper = new DefaultLagreDokumentRequestMapper();
		wsRequest = createWsLagreDokumentRequest();
	}

	@Test
	public void shouldMapFromWsRequestToDomainRequest() {
		domainRequest = lagreBrevRequestMapper.map(wsRequest);

		assertThat(domainRequest.isNewDocument(), is(NYTT_DOKUMENT));
		assertBrevStatus(domainRequest.getBrevStatus());
		assertBrev(domainRequest.getBrev());
	}

	private void assertBrevStatus(BrevStatusVO brevStatus) {
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getToken(), is(TOKEN));
		assertThat(brevStatus.getBrevmal(), is(MALPAKKE));
		assertThat(brevStatus.getReturKoe(), is(KVITTERINGSKOE));
	}

	private void assertBrev(BrevVO brev) {
		assertThat(brev.getSystemID(), is(SYSTEM_ID));
		assertThat(brev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brev.getContentType(), is(CONTENT_TYPE));
		assertThat(brev.getBrevdata(), is(DOKUMENTDATA));
		assertThat(brev.getBrukerID(), is(BRUKER_ID));
	}

	private LagreDokumentRequest createWsLagreDokumentRequest() {
		LagreDokumentRequest lagreDokumentRequest = new LagreDokumentRequest();
		lagreDokumentRequest.setBrevreferanse(BREVREFERANSE);
		lagreDokumentRequest.setToken(TOKEN);
		lagreDokumentRequest.setSystemId(SYSTEM_ID);
		lagreDokumentRequest.setBrukerId(BRUKER_ID);
		lagreDokumentRequest.setKvitteringskoe(KVITTERINGSKOE);
		lagreDokumentRequest.setMalpakke(MALPAKKE);
		lagreDokumentRequest.setNyttDokument(NYTT_DOKUMENT);
		lagreDokumentRequest.setDokumentData(new DataHandler(new ByteArrayDataSource(DOKUMENTDATA, CONTENT_TYPE)));
		return lagreDokumentRequest;
	}
}
