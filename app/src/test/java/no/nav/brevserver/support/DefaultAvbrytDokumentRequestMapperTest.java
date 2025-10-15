package no.nav.brevserver.support;

import no.nav.brevserver.nais.support.impl.DefaultAvbrytDokumentRequestMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultAvbrytDokumentRequestMapperTest {

	private static final String TOKEN = "TOKEN";
	private static final String BREVREFERANSE = "123";
	private static final String SYSTEM_ID = "PE2";

	private final DefaultAvbrytDokumentRequestMapper avbrytBrevRequestMapper = new DefaultAvbrytDokumentRequestMapper();;

	@Test
	public void shouldMapFromWsRequestToDomainRequest() {
		no.nav.brevserver.app.dokumentbehandling.to.AvbrytDokumentRequest domainRequest = avbrytBrevRequestMapper.map(createWsAvbrytDokumentRequest());

		var brevstatus = domainRequest.getBrevStatus();
		assertThat(brevstatus.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(brevstatus.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(brevstatus.getToken()).isEqualTo(TOKEN);
	}

	private AvbrytDokumentRequest createWsAvbrytDokumentRequest() {
		AvbrytDokumentRequest avbrytDokumentRequest = new AvbrytDokumentRequest();
		avbrytDokumentRequest.setBrevreferanse(BREVREFERANSE);
		avbrytDokumentRequest.setToken(TOKEN);
		avbrytDokumentRequest.setSystemId(SYSTEM_ID);
		return avbrytDokumentRequest;
	}

}