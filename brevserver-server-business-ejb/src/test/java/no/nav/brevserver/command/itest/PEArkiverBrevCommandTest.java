package no.nav.brevserver.command.itest;

import no.nav.brevserver.command.PEArkiverBrevCommand;
import no.nav.brevserver.consumer.joark.factory.JoarkServiceBeanFactory;
import no.nav.brevserver.consumer.joark.support.JoarkServiceBean;
import no.nav.brevserver.consumer.joark.util.JournalServiceTestdataUtils;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import no.nav.brevserver.service.jms.MessageProducer;
import no.nav.brevserver.service.jms.MessageProducerFactory;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalstatus;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostResponse;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.Journal;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Fildetaljer;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.OppdaterJournalRequest;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.Journalbehandling;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.OppdaterJournalUgyldigDokumentInfoId;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.OppdaterJournalUgyldigJournalpostId;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.OppdaterJournalUgyldigStatusovergang;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.xml.ws.BindingProvider;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Integration test of PEArkiverBrevCommand, external dependencies (db, jms, ws, etc.) are mocked.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigManager.class, BrevserverServiceFactory.class, JoarkServiceBeanFactory.class,
		MessageProducerFactory.class})
public class PEArkiverBrevCommandTest {

	@Mock
	private BrevserverService brevserverServiceMock;
	@Mock
	private MessageProducer messageProducerMock;
	@Mock
	private MessageVO messageMock;
	@Mock(extraInterfaces = BindingProvider.class)
	private Journalbehandling journalbehandlingServiceMock;
	@Captor
	private ArgumentCaptor<BrevStatusVO> brevStatusVOCaptor;
	@Captor
	private ArgumentCaptor<Brevstatus> brevstatusCaptor;
	@Captor
	private ArgumentCaptor<KvitteringVO> kvitteringCaptor;
	@Captor
	private ArgumentCaptor<OppdaterJournalRequest> oppdaterJournalRequestCaptor;

	private final String brevReferanse = "123123";
	private final String systemId = "PE2";
	private static final String[] JOURNALSTATUS_INVALID_LIST = {"A", "FS", "FL"};

	private Journalpost journalpost;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		mockConfigManager();
		setupBrevserverServiceMock();
		setupJoarkMocks();
		setupMessageProducerMock();
	}

	@Test
	public void shouldFailWhenJoarkJournalstatusIsInvalid() throws BrevTechnicalException {
		when(messageMock.getByteBody()).thenReturn(
				(createXmlKvitteringHeader(FilType.PDF.getContentType()) + "some pdf-content").getBytes());

		Journalstatus existingJournalstatus = journalpost.getJournalstatus();
		Journalstatus invalidJournalstatus = new Journalstatus();

		for (String journalstatus : JOURNALSTATUS_INVALID_LIST) {
			invalidJournalstatus.setKode(journalstatus);
			journalpost.setJournalstatus(invalidJournalstatus);

			try {
				new PEArkiverBrevCommand(messageMock).execute();
				fail();
			} catch (BrevException e) {
				assertTrue(e instanceof BrevTechnicalException);
				assertThat(e.getMessage(), containsString("Journalstatus '" + journalstatus + "' tillater ikke lagring/arkivering"));
			}
		}
		journalpost.setJournalstatus(existingJournalstatus);
	}

	@Test
	public void shouldUpdateStatusAfterSavingRtfDocument() throws Exception {
		when(messageMock.getByteBody()).thenReturn(
				(createXmlKvitteringHeader(FilType.RTF.getContentType())).getBytes());

		new PEArkiverBrevCommand(messageMock).execute();

		verify(messageProducerMock).sendKvittering(brevStatusVOCaptor.capture(), eq(messageMock), kvitteringCaptor.capture());

		assertThat(brevStatusVOCaptor.getValue().getStatus(), is(Konstanter.BREVSTATUS_LAGRET_KLADD));
		assertThat(kvitteringCaptor.getValue().getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_KLADD));
	}

	@Test
	public void shouldUpdateStatusAfterSavingDocxDocument() throws Exception {
		when(messageMock.getByteBody()).thenReturn(
				(createXmlKvitteringHeader(FilType.DOCX.getContentType())).getBytes());

		new PEArkiverBrevCommand(messageMock).execute();

		verify(messageProducerMock).sendKvittering(brevStatusVOCaptor.capture(), eq(messageMock), kvitteringCaptor.capture());

		assertThat(brevStatusVOCaptor.getValue().getStatus(), is(Konstanter.BREVSTATUS_LAGRET_KLADD));
		assertThat(kvitteringCaptor.getValue().getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_KLADD));
	}

	@Test
	public void shouldUpdateStatusAfterSavingPdfDocument() throws Exception {
		when(messageMock.getByteBody()).thenReturn(
				(createXmlKvitteringHeader(FilType.PDF.getContentType())).getBytes());

		new PEArkiverBrevCommand(messageMock).execute();

		verify(messageProducerMock).sendKvittering(brevStatusVOCaptor.capture(), eq(messageMock), kvitteringCaptor.capture());

		assertThat(brevStatusVOCaptor.getValue().getStatus(), is(Konstanter.BREVSTATUS_FERDIG));
		assertThat(kvitteringCaptor.getValue().getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_FERDIG));
	}

	@Test
	public void shouldSaveBrevStatusWithCorrectValues() throws Exception {
		when(messageMock.getByteBody()).thenReturn(
				(createXmlKvitteringHeader(FilType.PDF.getContentType()) + "some pdf-content").getBytes());
		String replyQueueName = "ReplyQueue";
		when(messageMock.getReplyQueueName()).thenReturn(replyQueueName);

		new PEArkiverBrevCommand(messageMock).execute();

		verify(brevserverServiceMock).lagreBrevStatus(brevstatusCaptor.capture(), any());

		Brevstatus brevStatus = brevstatusCaptor.getValue();
		assertThat(brevStatus.getBrevreferanse(), is(brevReferanse));
		assertThat(brevStatus.getSystemID(), is(systemId));
		assertThat(brevStatus.getReturKoe(), is(replyQueueName));
		assertThat(brevStatus.getStatus(), is(Konstanter.BREVSTATUS_FERDIG));
	}

	@Test
	public void shouldSavePdfCorrectlyInJoark() throws Exception {
		String brevData = "some pdf-content";
		when(messageMock.getByteBody()).thenReturn(
				(createXmlKvitteringHeader(FilType.PDF.getContentType()) + brevData).getBytes());

		executeAndAssertSavedFile(FilType.PDFA.getJoarkCode(), brevData);
	}

	@Test
	public void shouldSaveRtfCorrectlyInJoark() throws Exception {
		String brevData = "some pdf-content";
		when(messageMock.getByteBody()).thenReturn(
				(createXmlKvitteringHeader(FilType.RTF.getContentType()) + brevData).getBytes());

		executeAndAssertSavedFile(FilType.RTF.getJoarkCode(), brevData);
	}

	@Test
	public void shouldSaveDocxCorrectlyInJoark() throws Exception {
		String brevData = "some pdf-content";
		when(messageMock.getByteBody()).thenReturn(
				(createXmlKvitteringHeader(FilType.DOCX.getContentType()) + brevData).getBytes());

		executeAndAssertSavedFile(FilType.DOCX.getJoarkCode(), brevData);
	}

	private void executeAndAssertSavedFile(String filtype, String brevData) throws BrevException, OppdaterJournalUgyldigDokumentInfoId, OppdaterJournalUgyldigJournalpostId, OppdaterJournalUgyldigStatusovergang {
		new PEArkiverBrevCommand(messageMock).execute();

		verify(journalbehandlingServiceMock).oppdaterJournal(oppdaterJournalRequestCaptor.capture());

		List<Fildetaljer> fildetaljerListe = oppdaterJournalRequestCaptor.getValue().getJournalpostDokumentInfoRelasjonListe()
				.get(0).getDokumentInfo().getFildetaljerListe();
		for (Fildetaljer fildetaljer : fildetaljerListe) {
			if (fildetaljer.getFiltypeKode().equals(filtype)) {
				assertThat(fildetaljer.getFil(), is(brevData.getBytes()));
				return;
			}
		}
		fail("Filtype " + filtype + " is missing from the journalpost");
	}

	private void mockConfigManager() {
		ConfigManager configManagerMock = mock(ConfigManager.class);
		mockStatic(ConfigManager.class);
		when(ConfigManager.getInstance()).thenReturn(configManagerMock);
		when(configManagerMock.getInt(ConfigManager.ARKIVER_HEADER_LENGDE, Konstanter.MELDING_HEADER_LENGTH)).thenReturn(
				Konstanter.MELDING_HEADER_LENGTH);
	}

	private String createXmlKvitteringHeader(String contentType) {
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
		builder.append("<rtv-brevkvitt>");
		builder.append("<brevref>").append(brevReferanse).append("</brevref>");
		builder.append("<sysid>").append(systemId).append("</sysid>");
		builder.append("<type>").append(contentType).append("</type>");
		builder.append("<feilniva>").append("0").append("</feilniva>");
		builder.append("<feilkode>").append("0").append("</feilkode>");
		builder.append("</rtv-brevkvitt>");
		return StringUtils.rightPad(builder.toString(), Konstanter.MELDING_HEADER_LENGTH, ' ');
	}

	private void setupBrevserverServiceMock() {
		mockStatic(BrevserverServiceFactory.class);
		BrevserverServiceFactory brevserverServiceFactoryMock = mock(BrevserverServiceFactory.class);
		when(BrevserverServiceFactory.getInstance()).thenReturn(brevserverServiceFactoryMock);
		when(brevserverServiceFactoryMock.createBrevserverService()).thenReturn(brevserverServiceMock);
	}

	private void setupJoarkMocks() throws Exception {
		mockStatic(JoarkServiceBeanFactory.class);
		JoarkServiceBeanFactory joarkServiceBeanFactoryMock = mock(JoarkServiceBeanFactory.class);
		when(JoarkServiceBeanFactory.getInstance()).thenReturn(joarkServiceBeanFactoryMock);

		Journal journalServiceMock = mock(Journal.class, withSettings().extraInterfaces(BindingProvider.class));

		JoarkServiceBean joarkServiceBean = new JoarkServiceBean();
		joarkServiceBean.setJournalService(journalServiceMock);
		joarkServiceBean.setJournalbehandlingService(journalbehandlingServiceMock);
		joarkServiceBean.initDelegates();
		when(joarkServiceBeanFactoryMock.getJoarkService()).thenReturn(joarkServiceBean);

		journalpost = JournalServiceTestdataUtils.createJournalpost();
		HentJournalpostResponse hentJournalpostResponse = new HentJournalpostResponse();
		hentJournalpostResponse.setJournalpost(journalpost);
		when(journalServiceMock.hentJournalpost(isA(HentJournalpostRequest.class))).thenReturn(hentJournalpostResponse);
	}

	private void setupMessageProducerMock() {
		mockStatic(MessageProducerFactory.class);
		MessageProducerFactory messageProducerFactoryMock = mock(MessageProducerFactory.class);
		when(MessageProducerFactory.getInstance()).thenReturn(messageProducerFactoryMock);
		when(messageProducerFactoryMock.createMessageProducer(SystemType.PE)).thenReturn(messageProducerMock);
	}

}
