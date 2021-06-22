package no.nav.brevserver.consumer.joark.support;

import no.nav.brevserver.consumer.joark.map.OppdaterJournalRequestMapper;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalstatus;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostResponse;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.Journal;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.DokumentInfo;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Fildetaljer;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.JournalpostDokumentInfoRelasjon;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.OppdaterJournalRequest;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.Journalbehandling;
import no.stelvio.common.context.support.RequestContextSetter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static no.nav.brevserver.consumer.joark.support.AbstractJoarkDelegate.VARIANT_FORMAT_ARKIV;
import static no.nav.brevserver.consumer.joark.support.AbstractJoarkDelegate.VARIANT_FORMAT_PRODUKSJON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LagreDokumentDelegate.
 *
 * @author Thomas Eugen Bjorge, Visma Sirius
 */
public class LagreDokumentDelegateTest {

	private LagreDokumentDelegate lagreDokumentDelegate;

	@Mock
	private OppdaterJournalRequestMapper oppdaterJournalRequestMapperMock;
	@Mock
	private Journal journalServiceMock;
	@Mock
	private Journalbehandling journalbehandlingServiceMock;
	@Captor
	ArgumentCaptor<HentJournalpostRequest> hentJournalpostRequestCaptor;
	@Captor
	ArgumentCaptor<OppdaterJournalRequest> oppdaterJournalRequestCaptor;

	private static final String brevreferanse = "123123";
	private static final byte[] BREVDATA_REDIGERBART = "RTF".getBytes();
	private static final byte[] BREVDATA_IKKE_REDIGERBART = "PDF".getBytes();
	private static final String JOURNALSTATUS_VALID = "U";
	private static final String[] JOURNALSTATUS_INVALID_LIST = {"A", "FS", "FL"};

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		lagreDokumentDelegate = new LagreDokumentDelegate();
		lagreDokumentDelegate.setOppdaterJournalRequestMapper(oppdaterJournalRequestMapperMock);
		lagreDokumentDelegate.setJournalService(journalServiceMock);
		lagreDokumentDelegate.setJournalbehandlingService(journalbehandlingServiceMock);
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void shouldCallHentJournalpostWithBrevreferanse() throws Exception {
		setupMockForHentJournalpostAndMapping(createOppdaterJournalRequest());

		try {
			lagreDokumentDelegate.lagreDokument(brevreferanse, null, BREVDATA_REDIGERBART);
		} catch (BrevTechnicalException e) {
			// Expected exception since we don't set up all input and mocks in this test
		}

		verify(journalServiceMock).hentJournalpost(hentJournalpostRequestCaptor.capture());

		assertThat(hentJournalpostRequestCaptor.getValue().getJournalpostId(), is(Long.valueOf(brevreferanse)));
	}

	@Test
	public void shouldUpdateBothRtfAndPdfForLagreFerdigstiltDokument() throws Exception {
		Fildetaljer rtfFildetaljer = createFildetaljer(VARIANT_FORMAT_PRODUKSJON, FilType.RTF.getJoarkCode());
		Fildetaljer pdfFildetaljer = createFildetaljer(VARIANT_FORMAT_ARKIV, FilType.PDF.getJoarkCode());
		setupMockForHentJournalpostAndMapping(createOppdaterJournalRequest(rtfFildetaljer, pdfFildetaljer));
		BrevVO redigerbartBrev = createRtfRedigerbartBrev();
		BrevVO ferdigstiltBrev = createIkkeRedigerbartBrev();

		lagreDokumentDelegate.lagreFerdigstiltDokument(brevreferanse, redigerbartBrev, ferdigstiltBrev);

		assertThat(rtfFildetaljer.getFil(), equalTo(BREVDATA_REDIGERBART));
		assertThat(rtfFildetaljer.getFiltypeKode(), is(FilType.RTF.getJoarkCode()));
		assertThat(pdfFildetaljer.getFil(), equalTo(BREVDATA_IKKE_REDIGERBART));
		assertThat(pdfFildetaljer.getFiltypeKode(), is(FilType.PDFA.getJoarkCode()));
	}

	@Test
	public void shouldUpdateDocxAndPdfForLagreFerdigstiltDokument() throws Exception {
		Fildetaljer rtfFildetaljer = createFildetaljer(VARIANT_FORMAT_PRODUKSJON, FilType.RTF.getJoarkCode());
		Fildetaljer pdfFildetaljer = createFildetaljer(VARIANT_FORMAT_ARKIV, FilType.PDF.getJoarkCode());
		setupMockForHentJournalpostAndMapping(createOppdaterJournalRequest(rtfFildetaljer, pdfFildetaljer));
		BrevVO redigerbartBrev = createDocxRedigerbartBrev();
		BrevVO ferdigstiltBrev = createIkkeRedigerbartBrev();

		lagreDokumentDelegate.lagreFerdigstiltDokument(brevreferanse, redigerbartBrev, ferdigstiltBrev);

		assertThat(rtfFildetaljer.getFiltypeKode(), is(FilType.DOCX.getJoarkCode()));
		assertThat(rtfFildetaljer.getFil(), equalTo(BREVDATA_REDIGERBART));
		assertThat(pdfFildetaljer.getFiltypeKode(), is(FilType.PDFA.getJoarkCode()));
		assertThat(pdfFildetaljer.getFil(), equalTo(BREVDATA_IKKE_REDIGERBART));
	}

	@Test
	public void shouldThrowExceptionForInvalidContentTypeInInput() throws Exception {
		setupMockForHentJournalpostAndMapping(createOppdaterJournalRequest());

		lagreDokumentAndAssertExceptionMessageWith("text/tull", "Ugyldig filType",
				"text/tull");
	}

	@Test
	public void shouldThrowExceptionForInvalidJournalstatus() throws Exception {
		for (String journalstatus : JOURNALSTATUS_INVALID_LIST) {
			setupMockForHentJournalpostAndMapping(createOppdaterJournalRequest(), journalstatus);
			lagreDokumentAndAssertExceptionMessageWith(FilType.PDF.getContentType(), journalstatus, "tillater ikke lagring");
		}
	}

	@Test
	public void shouldThrowExceptionWhenDokumentInfoNotFound() throws Exception {
		OppdaterJournalRequest request = createOppdaterJournalRequest();
		request.getJournalpostDokumentInfoRelasjonListe().iterator().next().setDokumentInfo(null);
		setupMockForHentJournalpostAndMapping(request);

		lagreDokumentAndAssertExceptionMessageWith(FilType.PDF.getContentType(), "Fant ikke DokumentInfo");
	}

	@Test
	public void shouldThrowExceptionWhenFilDetaljerNotFound() throws Exception {
		setupMockForHentJournalpostAndMapping(createOppdaterJournalRequest());

		lagreDokumentAndAssertExceptionMessageWith(FilType.PDF.getContentType(), "Fant ikke filDetaljer", FilType.PDF.getContentType());
	}

	@Test
	public void shouldAddBrevDataCorrectlyForRtf() throws Exception {
		Fildetaljer rtfFildetaljer = createFildetaljer(VARIANT_FORMAT_PRODUKSJON, FilType.RTF.getJoarkCode());

		setupMockForHentJournalpostAndMapping(createOppdaterJournalRequest(rtfFildetaljer));

		lagreDokumentDelegate.lagreDokument(brevreferanse, FilType.RTF.getContentType(), BREVDATA_REDIGERBART);

		verify(journalbehandlingServiceMock).oppdaterJournal(oppdaterJournalRequestCaptor.capture());

		assertBrevdataOnCorrectFilDetaljer(FilType.RTF.getJoarkCode());
	}


	@Test
	public void shouldAddBrevDataCorrectlyForDocxWhenExistingRtf() throws Exception {
		Fildetaljer rtfFildetaljer = createFildetaljer(VARIANT_FORMAT_PRODUKSJON, FilType.RTF.getJoarkCode());
		Fildetaljer pdfFildetaljer = createFildetaljer(VARIANT_FORMAT_ARKIV, FilType.PDF.getJoarkCode());

		setupMockForHentJournalpostAndMapping(createOppdaterJournalRequest(rtfFildetaljer, pdfFildetaljer));

		lagreDokumentDelegate.lagreDokument(brevreferanse, FilType.DOCX.getContentType(), BREVDATA_REDIGERBART);

		verify(journalbehandlingServiceMock).oppdaterJournal(oppdaterJournalRequestCaptor.capture());

		assertBrevdataOnCorrectFilDetaljer(FilType.DOCX.getJoarkCode());
	}

	@Test
	public void shouldAddBrevDataCorrectlyForDocxWhenExistingDocx() throws Exception {
		Fildetaljer docxFildetaljer = createFildetaljer(VARIANT_FORMAT_PRODUKSJON, FilType.DOCX.getJoarkCode());
		Fildetaljer pdfFildetaljer = createFildetaljer(VARIANT_FORMAT_ARKIV, FilType.PDF.getJoarkCode());

		setupMockForHentJournalpostAndMapping(createOppdaterJournalRequest(docxFildetaljer, pdfFildetaljer));

		lagreDokumentDelegate.lagreDokument(brevreferanse, FilType.DOCX.getContentType(), BREVDATA_REDIGERBART);

		verify(journalbehandlingServiceMock).oppdaterJournal(oppdaterJournalRequestCaptor.capture());

		assertBrevdataOnCorrectFilDetaljer(FilType.DOCX.getJoarkCode());
	}

	@Test
	public void shouldAddBrevDataAndUpdateFiltypeToPdfaForPdf() throws Exception {
		Fildetaljer docxFildetaljer = createFildetaljer(VARIANT_FORMAT_PRODUKSJON, FilType.DOCX.getJoarkCode());
		Fildetaljer pdfFildetaljer = createFildetaljer(VARIANT_FORMAT_ARKIV, FilType.PDF.getJoarkCode());

		setupMockForHentJournalpostAndMapping(createOppdaterJournalRequest(docxFildetaljer, pdfFildetaljer));

		lagreDokumentDelegate.lagreDokument(brevreferanse, FilType.PDF.getContentType(), BREVDATA_IKKE_REDIGERBART);

		verify(journalbehandlingServiceMock).oppdaterJournal(oppdaterJournalRequestCaptor.capture());

		assertThat(pdfFildetaljer.getFil(), is(BREVDATA_IKKE_REDIGERBART));
		assertThat(pdfFildetaljer.getFiltypeKode(), is(FilType.PDFA.getJoarkCode()));
	}

	private void lagreDokumentAndAssertExceptionMessageWith(String contentType, String... exceptionMessages) {
		try {
			lagreDokumentDelegate.lagreDokument(brevreferanse, contentType, BREVDATA_REDIGERBART);
			fail();
		} catch (BrevTechnicalException e) {
			for (String message : exceptionMessages) {
				assertThat(e.getMessage(), containsString(message));

			}
		}
	}

	private void assertBrevdataOnCorrectFilDetaljer(String filtypeRedigerbar) {
		OppdaterJournalRequest request = oppdaterJournalRequestCaptor.getValue();
		DokumentInfo dokumentInfo = request.getJournalpostDokumentInfoRelasjonListe().iterator().next().getDokumentInfo();
		for (Fildetaljer fildetaljer : dokumentInfo.getFildetaljerListe()) {
			if (fildetaljer.getFiltypeKode().equals(filtypeRedigerbar)) {
				assertThat(fildetaljer.getFil(), is(BREVDATA_REDIGERBART));
			} else {
				assertThat(fildetaljer.getFil(), is(nullValue()));
			}
		}
	}

	private Fildetaljer createFildetaljer(String variantFormat, String filtype) {
		Fildetaljer pdfFildetaljer = new Fildetaljer();
		pdfFildetaljer.setVariantFormatKode(variantFormat);
		pdfFildetaljer.setFiltypeKode(filtype);
		return pdfFildetaljer;
	}

	private void setupMockForHentJournalpostAndMapping(OppdaterJournalRequest oppdaterJournalRequest)
			throws HentJournalpostJournalpostIkkeFunnet {
		setupMockForHentJournalpostAndMapping(oppdaterJournalRequest, JOURNALSTATUS_VALID);
	}

	private void setupMockForHentJournalpostAndMapping(OppdaterJournalRequest oppdaterJournalRequest, String journalstatusKode)
			throws HentJournalpostJournalpostIkkeFunnet {
		Journalpost journalpost = new Journalpost();
		Journalstatus journalstatus = new Journalstatus();
		journalstatus.setKode(journalstatusKode); // Under produksjon
		journalpost.setJournalstatus(journalstatus);
		HentJournalpostResponse hentJournalpostResponse = new HentJournalpostResponse();
		hentJournalpostResponse.setJournalpost(journalpost);
		when(journalServiceMock.hentJournalpost(isA(HentJournalpostRequest.class))).thenReturn(hentJournalpostResponse);
		when(oppdaterJournalRequestMapperMock.map(journalpost)).thenReturn(oppdaterJournalRequest);
	}

	private OppdaterJournalRequest createOppdaterJournalRequest(Fildetaljer... fildetaljer) {
		OppdaterJournalRequest request = new OppdaterJournalRequest();

		DokumentInfo dokumentInfo = new DokumentInfo();
		for (int i = 0; i < fildetaljer.length; i++) {
			dokumentInfo.getFildetaljerListe().add(fildetaljer[i]);
		}

		JournalpostDokumentInfoRelasjon dokumentInfoRelasjon = new JournalpostDokumentInfoRelasjon();
		dokumentInfoRelasjon.setDokumentInfo(dokumentInfo);

		request.getJournalpostDokumentInfoRelasjonListe().add(dokumentInfoRelasjon);
		return request;
	}

	private BrevVO createIkkeRedigerbartBrev() {
		BrevVO ferdigstiltBrev = new BrevVO();
		ferdigstiltBrev.setBrevdata(BREVDATA_IKKE_REDIGERBART);
		ferdigstiltBrev.setContentType(FilType.PDF.getContentType());
		return ferdigstiltBrev;
	}

	private BrevVO createRtfRedigerbartBrev() {
		BrevVO redigerbartBrev = new BrevVO();
		redigerbartBrev.setBrevdata(BREVDATA_REDIGERBART);
		redigerbartBrev.setContentType(FilType.RTF.getContentType());
		return redigerbartBrev;
	}

	private BrevVO createDocxRedigerbartBrev() {
		BrevVO redigerbartBrev = new BrevVO();
		redigerbartBrev.setBrevdata(BREVDATA_REDIGERBART);
		redigerbartBrev.setContentType(FilType.DOCX.getContentType());
		return redigerbartBrev;
	}
}
