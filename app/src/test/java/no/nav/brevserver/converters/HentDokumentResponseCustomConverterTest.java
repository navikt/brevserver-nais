package no.nav.brevserver.converters;

import no.nav.brevserver.app.dokumentbehandling.to.HentDokumentResponse;
import no.nav.brevserver.provider.map.converters.HentDokumentResponseCustomConverter;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for HentDokumentResponseCustomConverter
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class HentDokumentResponseCustomConverterTest {

	private static final byte[] DOKUMENTDATA = "Dokument".getBytes();
	private static final String CONTENT_TYPE = FilType.RTF.getContentType();
	private static final String KNAPPSTATUS = "1";

	private HentDokumentResponseCustomConverter hentDokumentResponseCustomConverter;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		hentDokumentResponseCustomConverter = new HentDokumentResponseCustomConverter();
	}

	@Test
	public void shouldConvertFromDomainResponseToWsResponse() throws IOException {
		HentDokumentResponse domainResponse = createDomainHentDokumentResponse();
		HentDokumentResponse2 wsResponse = hentDokumentResponseCustomConverter.convertTo(domainResponse, null);

		assertThat(wsResponse.getKnappStatus(), is(KNAPPSTATUS));
		assertThat(wsResponse.getDokumentData().getContentType(), is(CONTENT_TYPE));
		assertThat(IOUtils.toByteArray(wsResponse.getDokumentData().getInputStream()), is(DOKUMENTDATA));
	}

	@Test
	public void shouldThrowExceptionIfConvertFromWsToDomainIsCalled() {
		thrown.expect(UnsupportedOperationException.class);
		thrown.expectMessage("Convert HentDokumentResponse from WS to domain is not supported");

		hentDokumentResponseCustomConverter.convertFrom(new HentDokumentResponse2(), null);
	}

	private HentDokumentResponse createDomainHentDokumentResponse() {
		HentDokumentResponse domainResponse = new HentDokumentResponse();
		domainResponse.setContentType(CONTENT_TYPE);
		domainResponse.setKnappStatus(KNAPPSTATUS);
		domainResponse.setDokumentData(DOKUMENTDATA);
		return domainResponse;
	}
}
