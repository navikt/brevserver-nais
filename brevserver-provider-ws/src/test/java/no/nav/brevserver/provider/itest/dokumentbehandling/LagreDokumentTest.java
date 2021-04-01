package no.nav.brevserver.provider.itest.dokumentbehandling;

import no.nav.brevserver.consumer.joark.util.JournalServiceTestdataUtils;
import no.nav.brevserver.provider.itest.AbstractProviderTest;
import no.nav.brevserver.provider.support.DokumentbehandlingProvider;
import no.nav.brevserver.querydsl.TBrevlager5;
import no.nav.brevserver.querydsl.TBrevstatus;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevRuntimeException;
import no.nav.brevserver.server.common.jms.JMSAccessor;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.DokumentbehandlingPortType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostResponse;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.DokumentInfo;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Fildetaljer;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.OppdaterJournalRequest;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Integration tests for lagreDokument operation dokumentbehandling
 *
 * @author Nabil Fario, Visma Consulting
 */
@PrepareForTest({JMSAccessor.class})
public class LagreDokumentTest extends AbstractProviderTest {

	private static final String BREVREFERANSE = "123123";
	private static final String TOKEN = "123";
	private static final String CONTENT_TYPE_RTF = FilType.RTF.getContentType();
	private static final String BRUKER_ID = "brukerID";
	private static final String KVITTERINGSKOE = "kvitteringsKoe";
	private static final String MALPAKKE = "malpakke";
	private static final boolean NYTT_DOKUMENT = true;
	private static final String SYSTEM_ID_PE = SystemType.PE.toString();
	private static final String SYSTEM_ID_BI = SystemType.BI.toString();
	private static final String FILTYPE_REDIGERBAR = FilType.RTF.getJoarkCode();
	private static final byte[] DOKUMENTDATA = "hello world".getBytes();
	private static final byte[] NYTT_DOKUMENT_DATA = "Heisann verden".getBytes();

	private DokumentbehandlingPortType dokumentBehandlingProvider;
	private LagreDokumentRequest lagreBidragDokumentRequest;
	private LagreDokumentRequest lagrePensjonDokumentRequest;

	@Captor
	ArgumentCaptor<OppdaterJournalRequest> oppdaterJournalRequestCaptor;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		insertBrevForBidragInDatabase();
		insertBrevForPensjonInDatabase();
		setupJMSAccessor();
		setupJournalPostMock();
		dokumentBehandlingProvider = new DokumentbehandlingProvider();
	}

	@Test
	public void shouldLagreBidragDokumentTilBrevlager() throws Exception {
		lagreBidragDokumentRequest = createLagreDokumentRequest(BREVREFERANSE, SYSTEM_ID_BI);
		dokumentBehandlingProvider.lagreDokument(lagreBidragDokumentRequest);

		TBrevstatus tBrevstatus = getBrevstatus(BREVREFERANSE, SYSTEM_ID_BI);
		assertThat(tBrevstatus.getStatus(), is(Konstanter.BREVSTATUS_LAGRET_KLADD));
		TBrevlager5 brevlager = getBrevlager(BREVREFERANSE, SYSTEM_ID_BI);
		assertThat(IOUtils.toByteArray(brevlager.getBrevdata().getBinaryStream()), is(NYTT_DOKUMENT_DATA));
	}

	@Test
	public void shouldLagrePensjonDokumentTilJoark() throws Exception {
		lagrePensjonDokumentRequest = createLagreDokumentRequest(BREVREFERANSE, SYSTEM_ID_PE);
		dokumentBehandlingProvider.lagreDokument(lagrePensjonDokumentRequest);

		verify(journalbehandlingServiceMock).oppdaterJournal(oppdaterJournalRequestCaptor.capture());
		assertBrevdataOnCorrectFilDetaljer();
	}

	@Test
	public void shouldThrowExceptionIfBidragDokumentDoesNotExist() {
		thrown.expect(BrevRuntimeException.class);
		thrown.expectMessage("Ugyldig JournalpostID 'TullOgToys' mottatt, kan ikke lagre i JOARK.");

		lagreBidragDokumentRequest = createLagreDokumentRequest("TullOgToys", SYSTEM_ID_PE);
		dokumentBehandlingProvider.lagreDokument(lagreBidragDokumentRequest);
	}

	private LagreDokumentRequest createLagreDokumentRequest(String brevRef, String systemId) {
		LagreDokumentRequest lagreDokumentRequest = new LagreDokumentRequest();

		lagreDokumentRequest.setBrevreferanse(brevRef);
		lagreDokumentRequest.setSystemId(systemId);
		lagreDokumentRequest.setToken(TOKEN);
		lagreDokumentRequest.setBrukerId(BRUKER_ID);
		lagreDokumentRequest.setKvitteringskoe(KVITTERINGSKOE);
		lagreDokumentRequest.setMalpakke(MALPAKKE);
		lagreDokumentRequest.setNyttDokument(NYTT_DOKUMENT);
		lagreDokumentRequest.setDokumentData(new DataHandler(new ByteArrayDataSource(NYTT_DOKUMENT_DATA, CONTENT_TYPE_RTF)));

		return lagreDokumentRequest;
	}

	private void insertBrevForBidragInDatabase() throws java.sql.SQLException {
		insertBrevtilgang(BREVREFERANSE, TOKEN, SYSTEM_ID_BI);
		insertBrevstatus(BREVREFERANSE, SYSTEM_ID_BI, Konstanter.BREVSTATUS_AVBRUTT);
		insertBrevlager(BREVREFERANSE, SYSTEM_ID_BI, Konstanter.BREVLAGER_STATUS_KLADD, CONTENT_TYPE_RTF, DOKUMENTDATA);
	}

	private void insertBrevForPensjonInDatabase() throws java.sql.SQLException {
		insertBrevtilgang(BREVREFERANSE, TOKEN, SYSTEM_ID_PE);
		insertBrevstatus(BREVREFERANSE, SYSTEM_ID_PE, Konstanter.BREVSTATUS_AVBRUTT);
	}

	private void setupJMSAccessor() throws Exception {
		mockStatic(JMSAccessor.class);
		JMSAccessor jmsAccessorMock = mock(JMSAccessor.class);
		TextMessage textMessageMock = mock(javax.jms.TextMessage.class);

		when(JMSAccessor.getAccessorUsingQueueName(isA(String.class))).thenReturn(jmsAccessorMock);
		when(JMSAccessor.getAccessorUsingQueueJndiName(isA(String.class))).thenReturn(jmsAccessorMock);
		when(jmsAccessorMock.createTextMessage(isA(String.class))).thenReturn(textMessageMock);
	}

	private void assertBrevdataOnCorrectFilDetaljer() {
		OppdaterJournalRequest request = oppdaterJournalRequestCaptor.getValue();
		DokumentInfo dokumentInfo = request.getJournalpostDokumentInfoRelasjonListe().iterator().next().getDokumentInfo();
		for (Fildetaljer fildetaljer : dokumentInfo.getFildetaljerListe()) {
			if (fildetaljer.getFiltypeKode().equals(FILTYPE_REDIGERBAR)) {
				Assert.assertThat(fildetaljer.getFil(), Matchers.is(NYTT_DOKUMENT_DATA));
			} else {
				Assert.assertThat(fildetaljer.getFil(), Matchers.is(nullValue()));
			}
		}
	}

	private void setupJournalPostMock() throws Exception {
		HentJournalpostResponse hentJournalpostResponse = new HentJournalpostResponse();
		hentJournalpostResponse.setJournalpost(JournalServiceTestdataUtils.createJournalpost());
		when(journalServiceMock.hentJournalpost(isA(HentJournalpostRequest.class))).thenReturn(hentJournalpostResponse);
	}
}
