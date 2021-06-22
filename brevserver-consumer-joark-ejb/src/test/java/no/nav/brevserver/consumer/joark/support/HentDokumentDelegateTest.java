package no.nav.brevserver.consumer.joark.support;

import no.nav.brevserver.consumer.joark.util.JournalServiceTestdataUtils;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentResponse;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostResponse;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.Journal;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for HentDokumentDelegate.
 *
 * @author Marius Thoring, Visma Sirius
 */
public class HentDokumentDelegateTest {

	private static final byte[] BREVDATA = "JOARK".getBytes();

	HentDokumentDelegate hentDokumentDelegate;
	Journal journalServiceMock;
	Journalpost journalpost;

	@Before
	public void setUp() throws Exception {
		hentDokumentDelegate = new HentDokumentDelegate();

		journalServiceMock = mock(Journal.class);
		journalpost = JournalServiceTestdataUtils.createJournalpost();

		hentDokumentDelegate.setJournalService(journalServiceMock);

		HentJournalpostResponse hentJournalpostResponse = new HentJournalpostResponse();
		hentJournalpostResponse.setJournalpost(journalpost);
		when(journalServiceMock.hentJournalpost(isA(HentJournalpostRequest.class))).thenReturn(hentJournalpostResponse);

		HentDokumentResponse hentDokumentResponse = new HentDokumentResponse();
		hentDokumentResponse.setDokument(BREVDATA);
		when(journalServiceMock.hentDokument(isA(HentDokumentRequest.class))).thenReturn(hentDokumentResponse);
	}

	@Test
	public void shouldCallJournalServiceHentJournalpost() throws HentJournalpostJournalpostIkkeFunnet {
		try {
			hentDokumentDelegate.hentDokument("1");
		} catch (Exception e) {
		}
		ArgumentCaptor<HentJournalpostRequest> requestCaptor = ArgumentCaptor.forClass(HentJournalpostRequest.class);
		verify(journalServiceMock).hentJournalpost(requestCaptor.capture());
		assertThat(requestCaptor.getValue().getJournalpostId(), is(1L));
	}

	@Test
	public void shouldCallJournalServiceHentDokument() throws Exception {
		try {
			hentDokumentDelegate.hentDokument("1");
		} catch (Exception e) {
		}
		ArgumentCaptor<HentDokumentRequest> requestCaptor = ArgumentCaptor.forClass(HentDokumentRequest.class);
		verify(journalServiceMock).hentDokument(requestCaptor.capture());
		assertThat(requestCaptor.getValue().getJournalpostId(), is(1L));
	}

	@Test
	public void isJournalpostShouldReturnTrueIfJournalpostExists() throws Exception {
		try {
			assertThat(hentDokumentDelegate.isJournalpost("0"), is(true));
		} catch (Exception e) {
		}
		ArgumentCaptor<HentJournalpostRequest> requestCaptor = ArgumentCaptor.forClass(HentJournalpostRequest.class);
		verify(journalServiceMock).hentJournalpost(requestCaptor.capture());
		assertThat(requestCaptor.getValue().getJournalpostId(), is(0L));
	}

	@Test
	public void isJournalpostShouldReturnFalseIfIkkeFunnetException() throws Exception {
		HentJournalpostJournalpostIkkeFunnet ikkeFunnetException = mock(HentJournalpostJournalpostIkkeFunnet.class);
		when(journalServiceMock.hentJournalpost(isA(HentJournalpostRequest.class))).thenThrow(ikkeFunnetException);
		assertThat(hentDokumentDelegate.isJournalpost("0"), is(false));
	}

	@Test
	public void isJournalpostShouldThrowOtherExceptions() throws HentJournalpostJournalpostIkkeFunnet {
		when(journalServiceMock.hentJournalpost(isA(HentJournalpostRequest.class))).thenThrow(new RuntimeException());
		try {
			hentDokumentDelegate.isJournalpost("0");
			fail("Should not get here!");
		} catch (BrevTechnicalException e) {
		}
	}

	@Test
	public void ShouldThrowBrevExceptionIfIllegalJournalpostId() {
		try {
			hentDokumentDelegate.createHentJournalpostRequest("ABC");
			fail("Should not get here!");
		} catch (BrevTechnicalException e) {
		}
	}

	@Test
	public void shouldGetFilUuidProduksjonWhenNoArkivExists() throws Exception {
		JournalServiceTestdataUtils.clearFildetaljerListe(journalpost);
		addRtf();

		hentDokumentDelegate.hentDokument("1");

		ArgumentCaptor<HentDokumentRequest> requestCaptor = ArgumentCaptor.forClass(HentDokumentRequest.class);
		verify(journalServiceMock).hentDokument(requestCaptor.capture());
		assertThat(requestCaptor.getValue().getFilUuId(), is("PRODUKSJON-FILUUID"));
	}

	@Test
	public void shouldGetFilUuidArkivWhenNoProduksjonExists() throws Exception {
		JournalServiceTestdataUtils.clearFildetaljerListe(journalpost);
		addPdf();

		hentDokumentDelegate.hentDokument("1");

		ArgumentCaptor<HentDokumentRequest> requestCaptor = ArgumentCaptor.forClass(HentDokumentRequest.class);
		verify(journalServiceMock).hentDokument(requestCaptor.capture());
		assertThat(requestCaptor.getValue().getFilUuId(), is("ARKIV-FILUUID"));
	}

	@Test
	public void shouldPreferArkivWhenBothProduksjonAndArkivExists() throws Exception {
		JournalServiceTestdataUtils.clearFildetaljerListe(journalpost);
		addRtf();
		addPdf();

		hentDokumentDelegate.hentDokument("1");

		ArgumentCaptor<HentDokumentRequest> requestCaptor = ArgumentCaptor.forClass(HentDokumentRequest.class);
		verify(journalServiceMock).hentDokument(requestCaptor.capture());
		assertThat(requestCaptor.getValue().getFilUuId(), is("ARKIV-FILUUID"));
	}

	@Test
	public void shouldIgnoreZeroSizedDocuments() {
		JournalServiceTestdataUtils.clearFildetaljerListe(journalpost);
		addRtf();
		journalpost.getJournalpostDokumentInfoRelasjonListe().iterator().next().getDokumentInfo().getFildetaljerListe()
				.iterator().next().setFilstorrelse(null);

		try {
			hentDokumentDelegate.hentDokument("1");
			fail("Should throw exception when no only zero sized Fildetaljer found");
		} catch (BrevTechnicalException e) {
			assertTrue(e.getMessage().contains("Fant ikke filUuid"));
		}
	}

	@Test
	public void shouldThrowExceptionWhenNoFilUuidProduksjonOrArkivExists() {
		JournalServiceTestdataUtils.clearFildetaljerListe(journalpost);

		try {
			hentDokumentDelegate.hentDokument("1");
			fail("Should throw exception when no Fildetaljer with VariantFormat = PRODUKSJON or ARKIV");
		} catch (BrevTechnicalException e) {
			assertTrue(e.getMessage().contains("Fant ikke filUuid"));
		}
	}

	@Test
	public void shouldReturnCorrectBrevVOForPdf() throws BrevTechnicalException {
		JournalServiceTestdataUtils.clearFildetaljerListe(journalpost);
		addDocx();
		addPdf();

		BrevVO brevVO = hentDokumentDelegate.hentDokument("1");
		assertThat(brevVO.getBrevreferanse(), is("1"));
		assertThat(brevVO.getLagerStatus(), is(journalpost.getJournalstatus().getKode()));
		assertThat(brevVO.getContentType(), is(FilType.PDF.getContentType()));
		assertThat(brevVO.getBrevdata(), is(BREVDATA));
	}

	@Test
	public void shouldReturnCorrectBrevVOForRtf() throws BrevTechnicalException {
		JournalServiceTestdataUtils.clearFildetaljerListe(journalpost);
		addRtf();

		BrevVO brevVO = hentDokumentDelegate.hentDokument("1");
		assertThat(brevVO.getBrevreferanse(), is("1"));
		assertThat(brevVO.getLagerStatus(), is(journalpost.getJournalstatus().getKode()));
		assertThat(brevVO.getContentType(), is(FilType.RTF.getContentType()));
		assertThat(brevVO.getBrevdata(), is(BREVDATA));
	}

	@Test
	public void shouldReturnCorrectBrevVOForDocx() throws BrevTechnicalException {
		JournalServiceTestdataUtils.clearFildetaljerListe(journalpost);
		addDocx();

		BrevVO brevVO = hentDokumentDelegate.hentDokument("1");
		assertThat(brevVO.getBrevreferanse(), is("1"));
		assertThat(brevVO.getLagerStatus(), is(journalpost.getJournalstatus().getKode()));
		assertThat(brevVO.getContentType(), is(FilType.DOCX.getContentType()));
		assertThat(brevVO.getBrevdata(), is(BREVDATA));
	}


	private void addRtf() {
		JournalServiceTestdataUtils.addFildetaljer(journalpost, "PRODUKSJON-FILUUID", "PRODUKSJON", FilType.RTF.getJoarkCode());
	}

	private void addDocx() {
		JournalServiceTestdataUtils.addFildetaljer(journalpost, "PRODUKSJON-FILUUID", "PRODUKSJON", FilType.DOCX.getJoarkCode());
	}

	private void addPdf() {
		JournalServiceTestdataUtils.addFildetaljer(journalpost, "ARKIV-FILUUID", "ARKIV", FilType.PDF.getJoarkCode());
	}
}
