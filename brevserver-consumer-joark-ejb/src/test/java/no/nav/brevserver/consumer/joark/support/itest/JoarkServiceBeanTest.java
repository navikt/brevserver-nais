package no.nav.brevserver.consumer.joark.support.itest;

import no.nav.brevserver.consumer.joark.support.JoarkServiceBean;
import no.nav.brevserver.consumer.joark.util.JournalServiceTestdataUtils;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Filtype;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentResponse;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostResponse;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.HentDokumentFilUuidFinnesIkke;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.HentDokumentJournalpostIkkeFunnet;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.Journal;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Fildetaljer;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.OppdaterJournalRequest;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.Journalbehandling;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.OppdaterJournalUgyldigDokumentInfoId;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.OppdaterJournalUgyldigJournalpostId;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.OppdaterJournalUgyldigStatusovergang;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.xml.ws.BindingProvider;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests of JoarkServiceBean with mocked out Joark WS calls.
 *
 * @author Marius Thøring, Visma Sirius
 */
public class JoarkServiceBeanTest {

	@Mock(extraInterfaces = BindingProvider.class)
	private Journal journalServiceMock;
	@Mock(extraInterfaces = BindingProvider.class)
	private Journalbehandling journalbehandlingServiceMock;

	private JoarkServiceBean joarkService;
	private Journalpost journalpost;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		joarkService = new JoarkServiceBean();
		joarkService.setJournalService(journalServiceMock);
		joarkService.setJournalbehandlingService(journalbehandlingServiceMock);
		joarkService.initDelegates();

		mockHentJournalpost();
	}

	@Test
	public void shouldCallCorrectServicesForHentDokument() throws Exception {
		mockHentDokument();

		joarkService.hentDokument("1");

		verify(journalServiceMock).hentJournalpost(isA(HentJournalpostRequest.class));
		verify(journalServiceMock).hentDokument(isA(HentDokumentRequest.class));
	}

	@Test
	public void shouldCallCorrectServicesForLagreDokument() throws Exception {
		byte[] data = new byte[]{'N', 'Y'};
		joarkService.lagreDokument("1", FilType.RTF.getContentType(), data);

		verify(journalServiceMock).hentJournalpost(isA(HentJournalpostRequest.class));

		List<Fildetaljer> fildetaljerListe = getFildetaljerFromRequest();

		assertThat(fildetaljerListe.size(), is(2));
		assertThat(fildetaljerListe.iterator().next().getFil(), is(data));
	}

	@Test
	public void shouldHandleDocxIfExistingFildetaljerIsRtf() throws Exception {
		byte[] data = new byte[]{'N', 'Y'};
		joarkService.lagreDokument("1", FilType.DOCX.getContentType(), data);

		verify(journalServiceMock).hentJournalpost(isA(HentJournalpostRequest.class));
		List<Fildetaljer> fildetaljerListe = getFildetaljerFromRequest();

		assertThat(fildetaljerListe.size(), is(2));
		Fildetaljer fildetaljer = fildetaljerListe.iterator().next();
		assertThat(fildetaljer.getFiltypeKode(), is(FilType.DOCX.getJoarkCode()));
		assertThat(fildetaljer.getFil(), is(data));
	}

	@Test
	public void shouldHandleDocxIfExistingFildetaljerIsDocx() throws Exception {
		Filtype filtype = new Filtype();
		filtype.setKode(FilType.DOCX.getJoarkCode());
		journalpost.getJournalpostDokumentInfoRelasjonListe().iterator().next().getDokumentInfo()
				.getFildetaljerListe().iterator().next().setFiltype(filtype);

		byte[] data = new byte[]{'N', 'Y'};
		joarkService.lagreDokument("1", FilType.DOCX.getContentType(), data);

		verify(journalServiceMock).hentJournalpost(isA(HentJournalpostRequest.class));
		List<Fildetaljer> fildetaljerListe = getFildetaljerFromRequest();

		assertThat(fildetaljerListe.size(), is(2));
		Fildetaljer fildetaljer = fildetaljerListe.iterator().next();
		assertThat(fildetaljer.getFiltypeKode(), is(FilType.DOCX.getJoarkCode()));
		assertThat(fildetaljer.getFil(), is(data));
	}

	@Test
	public void shouldCallCorrectServicesForLagreFerdigstiltDokument() throws Exception {
		byte[] dataRtf = "rtf".getBytes();
		byte[] dataPdf = "pdf".getBytes();
		BrevVO redigerbartBrev = new BrevVO();
		redigerbartBrev.setBrevdata(dataRtf);
		redigerbartBrev.setContentType(FilType.RTF.getContentType());
		BrevVO ferdigstiltBrev = new BrevVO();
		ferdigstiltBrev.setBrevdata(dataPdf);
		ferdigstiltBrev.setContentType(FilType.PDF.getContentType());
		joarkService.lagreFerdigstiltDokument("1", redigerbartBrev, ferdigstiltBrev);

		verify(journalServiceMock).hentJournalpost(isA(HentJournalpostRequest.class));

		List<Fildetaljer> fildetaljerListe = getFildetaljerFromRequest();

		assertThat(fildetaljerListe.size(), is(2));
		assertThat(fildetaljerListe.get(0).getFil(), is(dataRtf));
		assertThat(fildetaljerListe.get(1).getFil(), is(dataPdf));
	}

	private void mockHentJournalpost() throws HentJournalpostJournalpostIkkeFunnet {
		HentJournalpostResponse hentJournalpostResponse = new HentJournalpostResponse();
		journalpost = JournalServiceTestdataUtils.createJournalpost();
		hentJournalpostResponse.setJournalpost(journalpost);
		when(journalServiceMock.hentJournalpost(isA(HentJournalpostRequest.class))).thenReturn(hentJournalpostResponse);
	}

	private void mockHentDokument() throws HentDokumentFilUuidFinnesIkke, HentDokumentJournalpostIkkeFunnet {
		HentDokumentResponse hentDokumentResponse = new HentDokumentResponse();
		hentDokumentResponse.setDokument(new byte[]{'J', 'O', 'A', 'R', 'K'});
		when(journalServiceMock.hentDokument(isA(HentDokumentRequest.class))).thenReturn(hentDokumentResponse);
	}

	private List<Fildetaljer> getFildetaljerFromRequest() throws OppdaterJournalUgyldigDokumentInfoId,
			OppdaterJournalUgyldigJournalpostId, OppdaterJournalUgyldigStatusovergang {
		ArgumentCaptor<OppdaterJournalRequest> captor = ArgumentCaptor.forClass(OppdaterJournalRequest.class);
		verify(journalbehandlingServiceMock).oppdaterJournal(captor.capture());
		List<Fildetaljer> fildetaljerListe = captor.getValue().getJournalpostDokumentInfoRelasjonListe().iterator().next()
				.getDokumentInfo().getFildetaljerListe();
		return fildetaljerListe;
	}
}
