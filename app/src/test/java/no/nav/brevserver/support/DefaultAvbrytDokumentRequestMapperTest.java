package no.nav.brevserver.support;

import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.nais.support.impl.DefaultAvbrytDokumentRequestMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for DefaultAvbrytDokumentRequestMapper
 */
public class DefaultAvbrytDokumentRequestMapperTest {
	private static final String TOKEN = "TOKEN";
	private static final String BREVREFERANSE = "123";
	private static final String SYSTEM_ID = "PE2";

	private DefaultAvbrytDokumentRequestMapper avbrytBrevRequestMapper;
	private AvbrytDokumentRequest wsRequest;
	private no.nav.brevserver.app.dokumentbehandling.to.AvbrytDokumentRequest domainRequest;

	@BeforeEach
	public void setUp() {
		avbrytBrevRequestMapper = new DefaultAvbrytDokumentRequestMapper();
		wsRequest = createWsAvbrytDokumentRequest();
	}

	@Test
	public void shouldMapFromWsRequestToDomainRequest() {
		domainRequest = avbrytBrevRequestMapper.map(wsRequest);
		assertBrevStatus(domainRequest.getBrevStatus());
	}

	private void assertBrevStatus(BrevStatusVO brevStatus) {
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getToken(), is(TOKEN));
	}

	private AvbrytDokumentRequest createWsAvbrytDokumentRequest() {
		AvbrytDokumentRequest avbrytDokumentRequest = new AvbrytDokumentRequest();
		avbrytDokumentRequest.setBrevreferanse(BREVREFERANSE);
		avbrytDokumentRequest.setToken(TOKEN);
		avbrytDokumentRequest.setSystemId(SYSTEM_ID);
		return avbrytDokumentRequest;
	}
}
