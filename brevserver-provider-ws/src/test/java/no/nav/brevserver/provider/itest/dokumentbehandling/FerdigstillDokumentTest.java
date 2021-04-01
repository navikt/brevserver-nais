package no.nav.brevserver.provider.itest.dokumentbehandling;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import no.nav.brevserver.consumer.joark.util.JournalServiceTestdataUtils;
import no.nav.brevserver.provider.itest.AbstractProviderTest;
import no.nav.brevserver.provider.support.DokumentbehandlingProvider;
import no.nav.brevserver.querydsl.TBrevlager5;
import no.nav.brevserver.querydsl.TBrevstatus;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.jms.JMSAccessor;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.DokumentbehandlingPortType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostResponse;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.DokumentInfo;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Fildetaljer;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.OppdaterJournalRequest;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.powermock.core.classloader.annotations.PrepareForTest;

import javax.activation.DataHandler;
import javax.jms.TextMessage;
import javax.mail.util.ByteArrayDataSource;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Integration tests for avbrytDokument operation dokumentbehandling
 *
 * @author Nabil Fario, Visma Consulting
 */
@PrepareForTest({JMSAccessor.class})
public class FerdigstillDokumentTest extends AbstractProviderTest {

	private static final String FILTYPE_IKKE_REDIGERBAR_ARKIV = FilType.PDFA.getJoarkCode();
	private static final String FILTYPE_REDIGERBAR = FilType.RTF.getJoarkCode();

	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";
	private static final String SYSTEM_ID_PE = SystemType.PE.toString();
	private static final String SYSTEM_ID_BI = SystemType.BI.toString();
	private static final String BRUKER_ID = "brukerID";
	private static final String KVITTERINGSKOE = "kvitteringsKoe";
	private static final String MALPAKKE = "malpakke";
	private static final boolean NYTT_DOKUMENT = true;
	private static final String CONTENT_TYPE_PDF = FilType.PDF.getContentType();
	private static final String CONTENT_TYPE_RTF = FilType.RTF.getContentType();
	private static final byte[] DOKUMENTDATA_RTF = "hello rtf".getBytes();
	private static final byte[] DOKUMENTDATA_FERDIG_RTF = "Ferdig rtf".getBytes();
	private static final byte[] DOKUMENTDATA_FERDIG_PDF = "Ferdig pdf".getBytes();

	private DokumentbehandlingPortType dokumentBehandlingProvider;
	private FerdigstillDokumentRequest ferdigstillDokumentRequest;

	@Captor
	ArgumentCaptor<OppdaterJournalRequest> oppdaterJournalRequestCaptor;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		setupJournalPostMock();
		setupJMSAccessor();
		insertBidragBrevInDatabase();
		insertPensjonBrevInDatabase();
		dokumentBehandlingProvider = new DokumentbehandlingProvider();
	}

	@Test
	public void shouldFerdigstilleDokumentForBidrag() throws Exception {
		ferdigstillDokumentRequest = createFerdigstillDokumentRequest(SYSTEM_ID_BI);

		dokumentBehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);

		TBrevstatus tBrevstatus = getBrevstatus(BREVREFERANSE, SYSTEM_ID_BI);
		assertThat(tBrevstatus.getStatus(), is(Konstanter.BREVSTATUS_FERDIG));
		TBrevlager5 brevlager = getBrevlager(BREVREFERANSE, SYSTEM_ID_BI);
		assertThat(brevlager.getStatus(), is(Konstanter.BREVLAGER_STATUS_FERDIG));
		assertThat(brevlager.getContenttype(), is(CONTENT_TYPE_PDF));
		assertThat(IOUtils.toByteArray(brevlager.getBrevdata().getBinaryStream()), is(DOKUMENTDATA_FERDIG_PDF));
	}

	@Test
	public void shouldFerdigstilleDokumentForJoark() throws Exception {
		ferdigstillDokumentRequest = createFerdigstillDokumentRequest(SYSTEM_ID_PE);

		dokumentBehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);

		verify(journalbehandlingServiceMock).oppdaterJournal(oppdaterJournalRequestCaptor.capture());

		assertRtfAndPdfDokumentData();
		TBrevstatus tBrevstatus = getBrevstatus(BREVREFERANSE, SYSTEM_ID_PE);
		assertThat(tBrevstatus.getStatus(), is(Konstanter.BREVSTATUS_FERDIG));
	}

	@Test
	public void shouldThrowExceptionIfValidationFails() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("brevStatus.systemID must be set");

		dokumentBehandlingProvider.ferdigstillDokument(new FerdigstillDokumentRequest());
	}

	@Test
	public void shouldThrowExceptionIfKvitteringskoeMissingAndNewDokumentSet() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("brevStatus.returKoe must be set if newDocument is true");

		FerdigstillDokumentRequest ferdigstillDokumentRequest = createFerdigstillDokumentRequest(SYSTEM_ID_BI);
		ferdigstillDokumentRequest.setNyttDokument(true);
		ferdigstillDokumentRequest.setKvitteringskoe(null);

		dokumentBehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);
	}

	@Test
	public void shouldThrowExceptionIfMalpakkeMissingAndNewDokumentSet() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("brevStatus.brevmal must be set if newDocument is true");

		FerdigstillDokumentRequest ferdigstillDokumentRequest = createFerdigstillDokumentRequest(SYSTEM_ID_BI);
		ferdigstillDokumentRequest.setNyttDokument(true);
		ferdigstillDokumentRequest.setMalpakke(null);

		dokumentBehandlingProvider.ferdigstillDokument(ferdigstillDokumentRequest);
	}

	private FerdigstillDokumentRequest createFerdigstillDokumentRequest(String systemId) {
		FerdigstillDokumentRequest ferdigstillDokumentRequest = new FerdigstillDokumentRequest();
		ferdigstillDokumentRequest.setBrevreferanse(BREVREFERANSE);
		ferdigstillDokumentRequest.setSystemId(systemId);
		ferdigstillDokumentRequest.setToken(TOKEN);
		ferdigstillDokumentRequest.setBrukerId(BRUKER_ID);
		ferdigstillDokumentRequest.setKvitteringskoe(KVITTERINGSKOE);
		ferdigstillDokumentRequest.setMalpakke(MALPAKKE);
		ferdigstillDokumentRequest.setRedDokument(new DataHandler(new ByteArrayDataSource(DOKUMENTDATA_FERDIG_RTF, CONTENT_TYPE_RTF)));
		ferdigstillDokumentRequest.setPdfDokument(new DataHandler(new ByteArrayDataSource(DOKUMENTDATA_FERDIG_PDF, CONTENT_TYPE_PDF)));
		ferdigstillDokumentRequest.setNyttDokument(NYTT_DOKUMENT);

		return ferdigstillDokumentRequest;
	}

	private void assertRtfAndPdfDokumentData() {
		OppdaterJournalRequest request = oppdaterJournalRequestCaptor.getValue();
		DokumentInfo dokumentInfo = request.getJournalpostDokumentInfoRelasjonListe().iterator().next().getDokumentInfo();
		assertThat(dokumentInfo.getFildetaljerListe().size(), is(2));
		List<Fildetaljer> fildetaljer = dokumentInfo.getFildetaljerListe();
		Fildetaljer fildetaljRtf = findFildetalj(FILTYPE_REDIGERBAR, fildetaljer);
		Fildetaljer fildetaljPdf = findFildetalj(FILTYPE_IKKE_REDIGERBAR_ARKIV, fildetaljer);

		assertThat(fildetaljRtf.getFil(), is(DOKUMENTDATA_FERDIG_RTF));
		assertThat(fildetaljPdf.getFil(), is(DOKUMENTDATA_FERDIG_PDF));
	}

	private Fildetaljer findFildetalj(final String filtype, List<Fildetaljer> fildetaljer) {
		return Iterables.find(fildetaljer, new Predicate<Fildetaljer>() {
			@Override
			public boolean apply(Fildetaljer fildetalj) {
				return fildetalj.getFiltypeKode().equals(filtype);
			}
		});
	}

	private void insertPensjonBrevInDatabase() throws java.sql.SQLException {
		insertBrevtilgang(BREVREFERANSE, TOKEN, SYSTEM_ID_PE);
		insertBrevstatus(BREVREFERANSE, SYSTEM_ID_PE, Konstanter.BREVSTATUS_LAGRET_KLADD);
	}

	private void insertBidragBrevInDatabase() throws java.sql.SQLException {
		insertBrevtilgang(BREVREFERANSE, TOKEN, SYSTEM_ID_BI);
		insertBrevstatus(BREVREFERANSE, SYSTEM_ID_BI, Konstanter.BREVSTATUS_LAGRET_KLADD);
		insertBrevlager(BREVREFERANSE, SYSTEM_ID_BI, Konstanter.BREVLAGER_STATUS_KLADD, CONTENT_TYPE_RTF, DOKUMENTDATA_RTF);
	}

	private void setupJMSAccessor() throws Exception {
		mockStatic(JMSAccessor.class);
		JMSAccessor jmsAccessorMock = mock(JMSAccessor.class);
		TextMessage textMessageMock = mock(javax.jms.TextMessage.class);

		when(JMSAccessor.getAccessorUsingQueueName(isA(String.class))).thenReturn(jmsAccessorMock);
		when(JMSAccessor.getAccessorUsingQueueJndiName(isA(String.class))).thenReturn(jmsAccessorMock);
		when(jmsAccessorMock.createTextMessage(isA(String.class))).thenReturn(textMessageMock);
	}

	private void setupJournalPostMock() throws Exception {
		HentJournalpostResponse hentJournalpostResponse = new HentJournalpostResponse();
		hentJournalpostResponse.setJournalpost(JournalServiceTestdataUtils.createJournalpost());
		when(journalServiceMock.hentJournalpost(isA(HentJournalpostRequest.class))).thenReturn(hentJournalpostResponse);
	}
}