package no.nav.brevserver.support;


import no.nav.brevserver.app.dokumentbehandling.to.HentDokumentResponse;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.nais.support.impl.DefaultHentDokumentResponseMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for DefaultHentDokumentResponseMapperTest
 */
public class DefaultHentDokumentResponseMapperTest {
	private static final String CONTENT_TYPE = FilType.RTF.getContentType();
	private static final String KNAPPSTATUS = "1";
	private static final byte[] DOKUMENTDATA = "Hallo verden".getBytes();

	private DefaultHentDokumentResponseMapper hentBrevResponseMapper;

	private HentDokumentResponse domainResponse;
	private HentDokumentResponse2 wsResponse;

	@BeforeEach
	public void setUp() {
		hentBrevResponseMapper = new DefaultHentDokumentResponseMapper();
		domainResponse = createDomainHentDokumentResponse();
	}

	@Test
	public void shouldMapFromDomainResponseToWsResponse() throws Exception {
		wsResponse = hentBrevResponseMapper.map(domainResponse);

		assertThat(IOUtils.toByteArray(wsResponse.getDokumentData().getInputStream()), is(DOKUMENTDATA));
		assertThat(wsResponse.getDokumentData().getContentType(), is(CONTENT_TYPE));
		assertThat(wsResponse.getKnappStatus(), is(KNAPPSTATUS));
	}

	private HentDokumentResponse createDomainHentDokumentResponse() {
		HentDokumentResponse hentDokumentResponse = new HentDokumentResponse();
		hentDokumentResponse.setContentType(CONTENT_TYPE);
		hentDokumentResponse.setKnappStatus(KNAPPSTATUS);
		hentDokumentResponse.setDokumentData(DOKUMENTDATA);
		return hentDokumentResponse;
	}
}
