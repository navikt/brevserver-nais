package no.nav.brevserver.support;

import no.nav.brevserver.nais.support.impl.DefaultHentDokumentRequestMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultHentDokumentRequestMapperTest {

	private static final String TOKEN = "TOKEN";
	private static final String BREVREFERANSE = "123";
	private static final String SYSTEM_ID = "PE2";

	private final DefaultHentDokumentRequestMapper hentBrevRequestMapper = new DefaultHentDokumentRequestMapper();

	@Test
	public void shouldMapFromWsRequestToDomainRequest() {
		no.nav.brevserver.app.dokumentbehandling.to.HentDokumentRequest domainRequest = hentBrevRequestMapper.map(createWsHentDokumentRequest());

		var brevstatus = domainRequest.getBrevStatus();
		assertThat(brevstatus.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(brevstatus.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(brevstatus.getToken()).isEqualTo(TOKEN);
	}

	private HentDokumentRequest createWsHentDokumentRequest() {
		HentDokumentRequest hentDokumentRequest = new HentDokumentRequest();
		hentDokumentRequest.setBrevreferanse(BREVREFERANSE);
		hentDokumentRequest.setToken(TOKEN);
		hentDokumentRequest.setSystemId(SYSTEM_ID);
		return hentDokumentRequest;
	}

}