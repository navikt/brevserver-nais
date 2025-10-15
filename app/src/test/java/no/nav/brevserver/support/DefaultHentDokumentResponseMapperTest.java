package no.nav.brevserver.support;

import no.nav.brevserver.app.dokumentbehandling.to.HentDokumentResponse;
import no.nav.brevserver.nais.support.impl.DefaultHentDokumentResponseMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import static no.nav.brevserver.core.vo.FilType.RTF;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultHentDokumentResponseMapperTest {

	private static final String CONTENT_TYPE = RTF.getContentType();
	private static final String KNAPPSTATUS = "1";
	private static final byte[] DOKUMENTDATA = "Hallo verden".getBytes();

	private final DefaultHentDokumentResponseMapper hentBrevResponseMapper = new DefaultHentDokumentResponseMapper();

	@Test
	public void shouldMapFromDomainResponseToWsResponse() throws Exception {
		HentDokumentResponse2 wsResponse = hentBrevResponseMapper.map(createDomainHentDokumentResponse());

		assertThat(IOUtils.toByteArray(wsResponse.getDokumentData().getInputStream())).isEqualTo(DOKUMENTDATA);
		assertThat(wsResponse.getDokumentData().getContentType()).isEqualTo(CONTENT_TYPE);
		assertThat(wsResponse.getKnappStatus()).isEqualTo(KNAPPSTATUS);
	}

	private HentDokumentResponse createDomainHentDokumentResponse() {
		HentDokumentResponse hentDokumentResponse = new HentDokumentResponse();
		hentDokumentResponse.setContentType(CONTENT_TYPE);
		hentDokumentResponse.setKnappStatus(KNAPPSTATUS);
		hentDokumentResponse.setDokumentData(DOKUMENTDATA);
		return hentDokumentResponse;
	}
}
