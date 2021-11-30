package no.nav.brevserver.support;

import no.nav.brevserver.nais.support.impl.DefaultHentDokumentRequestMapper;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for DefaultHentDokumentRequestMapper
 *
 * @author Joakim Bj�rnstad, Visma Consulting
 */
public class DefaultHentDokumentRequestMapperTest {
	private static final String TOKEN = "TOKEN";
	private static final String BREVREFERANSE = "123";
	private static final String SYSTEM_ID = "PE2";

	private DefaultHentDokumentRequestMapper hentBrevRequestMapper;
	private HentDokumentRequest wsRequest;
	private no.nav.brevserver.app.dokumentbehandling.to.HentDokumentRequest domainRequest;

	@Before
	public void setUp() {
		hentBrevRequestMapper = new DefaultHentDokumentRequestMapper();
		wsRequest = createWsHentDokumentRequest();
	}

	@Test
	public void shouldMapFromWsRequestToDomainRequest() {
		domainRequest = hentBrevRequestMapper.map(wsRequest);
		assertBrevStatus(domainRequest.getBrevStatus());
	}

	private void assertBrevStatus(BrevStatusVO brevStatus) {
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getToken(), is(TOKEN));
	}

	private HentDokumentRequest createWsHentDokumentRequest() {
		HentDokumentRequest hentDokumentRequest = new HentDokumentRequest();
		hentDokumentRequest.setBrevreferanse(BREVREFERANSE);
		hentDokumentRequest.setToken(TOKEN);
		hentDokumentRequest.setSystemId(SYSTEM_ID);
		return hentDokumentRequest;
	}
}
