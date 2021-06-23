package no.nav.brevserver.provider.itest.dokumentbehandling;

import no.nav.brevserver.consumer.joark.util.JournalServiceTestdataUtils;
import no.nav.brevserver.provider.itest.AbstractProviderTest;
import no.nav.brevserver.provider.support.DokumentbehandlingProvider;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevRuntimeException;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.DokumentbehandlingPortType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentResponse;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostResponse;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the hentDokument operation dokumentbehandling
 *
 * @author Joakim Bj�rnstad, Visma Consulting
 */
public class HentDokumentTest extends AbstractProviderTest {

	private static final String BREVREFERANSE_BIDRAG = "100";
	private static final String TOKEN_BIDRAG = "999";
	private static final String SYSTEM_BIDRAG = "BI12";

	private static final String BREVREFERANSE_PENSJON = "200";
	private static final String TOKEN_PENSJON = "888";
	private static final String SYSTEM_PENSJON = "PE2";

	private static final String KNAPPSTATUS = "7";
	private static final String CONTENT_TYPE = FilType.RTF.getContentType();
	private static final byte[] BREVDATA = "{\\rtf1 Dette er en test}".getBytes();

	private DokumentbehandlingPortType dokumentbehandlingProvider;

	private Journalpost journalpost;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		insertBrevInDatabase();
		dokumentbehandlingProvider = new DokumentbehandlingProvider();
	}

	@Test
	public void shouldHentBidragDokumentFraBrevlager() throws Exception {
		HentDokumentRequest hentDokumentRequest = createHentBidragDokumentRequest();
		HentDokumentResponse2 hentDokumentResponse = dokumentbehandlingProvider.hentDokument(hentDokumentRequest);

		assertThat(IOUtils.toByteArray(hentDokumentResponse.getDokumentData().getInputStream()), is(BREVDATA));
		assertThat(hentDokumentResponse.getKnappStatus(), is(KNAPPSTATUS));
	}

	@Test
	public void shouldHentPensjonDokumentFraJoark() throws Exception {
		mockJoarkRequestResponse();
		HentDokumentRequest hentDokumentRequest = createHentPensjonDokumentRequest();
		HentDokumentResponse2 hentDokumentResponse = dokumentbehandlingProvider.hentDokument(hentDokumentRequest);

		assertThat(IOUtils.toByteArray(hentDokumentResponse.getDokumentData().getInputStream()), is(BREVDATA));
		assertThat(hentDokumentResponse.getKnappStatus(), is(KNAPPSTATUS));
	}

	@Test
	public void shouldThrowExceptionIfDokumentDoesNotExist() {
		thrown.expect(BrevRuntimeException.class);
		thrown.expectMessage("Fant ikke dokumentet i Brevlageret");

		insertBrevtilgang("TullOgToys", TOKEN_BIDRAG, SYSTEM_BIDRAG);
		HentDokumentRequest hentDokumentRequest = createHentBidragDokumentRequest();
		hentDokumentRequest.setBrevreferanse("TullOgToys");

		dokumentbehandlingProvider.hentDokument(hentDokumentRequest);
	}

	private HentDokumentRequest createHentBidragDokumentRequest() {
		HentDokumentRequest hentDokumentRequest = new HentDokumentRequest();
		hentDokumentRequest.setBrevreferanse(BREVREFERANSE_BIDRAG);
		hentDokumentRequest.setToken(TOKEN_BIDRAG);
		hentDokumentRequest.setSystemId(SYSTEM_BIDRAG);
		return hentDokumentRequest;
	}

	private HentDokumentRequest createHentPensjonDokumentRequest() {
		HentDokumentRequest hentDokumentRequest = new HentDokumentRequest();
		hentDokumentRequest.setBrevreferanse(BREVREFERANSE_PENSJON);
		hentDokumentRequest.setToken(TOKEN_PENSJON);
		hentDokumentRequest.setSystemId(SYSTEM_PENSJON);
		return hentDokumentRequest;
	}

	private void mockJoarkRequestResponse() throws Exception {
		journalpost = JournalServiceTestdataUtils.createJournalpost();
		HentJournalpostResponse hentJournalpostResponse = new HentJournalpostResponse();
		hentJournalpostResponse.setJournalpost(journalpost);
		when(journalServiceMock.hentJournalpost(isA(HentJournalpostRequest.class))).thenReturn(hentJournalpostResponse);

		HentDokumentResponse hentDokumentResponse = new HentDokumentResponse();
		hentDokumentResponse.setDokument(BREVDATA);
		when(journalServiceMock.hentDokument(
				isA(no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentRequest.class)))
				.thenReturn(hentDokumentResponse);
	}

	private void insertBrevInDatabase() throws SQLException {
		insertBrevtilgang(BREVREFERANSE_BIDRAG, TOKEN_BIDRAG, SYSTEM_BIDRAG);
		insertBrevstatus(BREVREFERANSE_BIDRAG, SYSTEM_BIDRAG, Konstanter.BREVSTATUS_LAGRET_KLADD);
		insertBrevlager(BREVREFERANSE_BIDRAG, SYSTEM_BIDRAG, Konstanter.BREVLAGER_STATUS_KLADD, CONTENT_TYPE, BREVDATA);

		insertBrevtilgang(BREVREFERANSE_PENSJON, TOKEN_PENSJON, SYSTEM_PENSJON);
		insertBrevstatus(BREVREFERANSE_PENSJON, SYSTEM_PENSJON, Konstanter.BREVSTATUS_LAGRET_KLADD);
	}
}
