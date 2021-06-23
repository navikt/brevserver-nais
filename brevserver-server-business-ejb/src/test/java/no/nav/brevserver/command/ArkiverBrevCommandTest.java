package no.nav.brevserver.command;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.brevlager.BrevlagerService;
import no.nav.brevserver.service.brevlager.BrevlagerServiceFactory;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import no.nav.brevserver.service.jms.MessageProducer;
import no.nav.brevserver.service.jms.MessageProducerFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Unit tests for ArkiverBrevCommand
 *
 * @author Joakim Bj�rnstad, Visma Consulting
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigManager.class, BrevserverServiceFactory.class, BrevlagerServiceFactory.class,
		MessageProducerFactory.class, DialogueXMLParser.class})
public class ArkiverBrevCommandTest {

	@Mock
	private BrevserverService brevserverServiceMock;
	@Mock
	private BrevlagerService brevlagerServiceMock;
	@Mock
	private MessageProducer messageProducerMock;
	@Mock
	private MessageVO messageMock;
	@Captor
	private ArgumentCaptor<BrevStatusVO> brevStatusVOCaptor;
	@Captor
	private ArgumentCaptor<Brevstatus> brevstatusCaptor;
	@Captor
	private ArgumentCaptor<KvitteringVO> kvitteringCaptor;

	private final String brevReferanse = "123123";
	private final String systemId = "BI00";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private ArkiverBrevCommand arkiverBrevCommand;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockStatic(DialogueXMLParser.class);
		mockConfigManager();
		setupBrevserverServiceMock();
		setupBrevlagerMock();
		setupMessageProducerMock();

		arkiverBrevCommand = new ArkiverBrevCommand(messageMock);
	}

	@Test
	public void shouldThrowExceptionIfLagKvitteringFails() throws Exception {
		expectedException.expect(BrevTechnicalException.class);
		expectedException.expectMessage("Technical exception");

		when(DialogueXMLParser.lagKvitteringVOFraDialogueMelding(any())).thenThrow(new BrevTechnicalException("Technical exception"));

		Whitebox.<Void>invokeMethod(arkiverBrevCommand, "validate");
	}

	@Test
	public void shouldThrowExceptionIfSystemIDStartsWithPE() throws Exception {
		expectedException.expect(BrevTechnicalException.class);
		expectedException.expectMessage("Brev med feil systemID mottatt: 'PE02', forventet ikke pensjonsbrev");

		KvitteringVO kvittering = createDefaultKvittering();
		kvittering.setSystemID("PE02");
		when(DialogueXMLParser.lagKvitteringVOFraDialogueMelding(any())).thenReturn(kvittering);

		Whitebox.<Void>invokeMethod(arkiverBrevCommand, "validate");
	}

	@Test
	public void shouldSetBrevReferanseAfterValidating() throws Exception {
		when(DialogueXMLParser.lagKvitteringVOFraDialogueMelding(any())).thenReturn(createDefaultKvittering());

		Whitebox.<Void>invokeMethod(arkiverBrevCommand, "validate");

		Mockito.verify(messageMock).setBrevreferanse(brevReferanse);
	}

	@Test
	public void shouldLagreRtfKladdBrevIBrevlageret() throws Exception {
		KvitteringVO kvitt = createDefaultKvittering();
		kvitt.setContentType(FilType.RTF.getContentType());
		when(DialogueXMLParser.lagKvitteringVOFraDialogueMelding(any())).thenReturn(kvitt);
		when(brevserverServiceMock.hentBrevStatus(systemId, brevReferanse)).thenReturn(createDefaultBrevstatus());
		when(brevlagerServiceMock.lagreBrev(any(BrevVO.class), any(Brevstatus.class), any(String.class))).thenReturn(null);

		arkiverBrevCommand.execute();

		verify(brevlagerServiceMock).lagreBrev(kvitteringCaptor.capture(), brevstatusCaptor.capture(), any());
		verify(messageProducerMock).sendKvittering(brevStatusVOCaptor.getValue(), messageMock, kvitteringCaptor.getValue());
		KvitteringVO kvittering = kvitteringCaptor.getValue();
		BrevStatusVO brevStatus = brevStatusVOCaptor.getValue();

		assertThat(kvittering.getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_KLADD));
		assertThat(brevStatus.getStatus(), is(Konstanter.BREVSTATUS_LAGRET_KLADD));
	}

	@Test
	public void shouldLagrePdfFerdigBrevIBrevlageret() throws Exception {
		when(DialogueXMLParser.lagKvitteringVOFraDialogueMelding(any())).thenReturn(createDefaultKvittering());
		when(brevserverServiceMock.hentBrevStatus(systemId, brevReferanse)).thenReturn(createDefaultBrevstatus());
		when(brevlagerServiceMock.lagreBrev(any(BrevVO.class), any(Brevstatus.class), any(String.class))).thenReturn(null);

		arkiverBrevCommand.execute();

		verify(brevlagerServiceMock).lagreBrev(kvitteringCaptor.capture(), brevstatusCaptor.capture(), any());
		verify(messageProducerMock).sendKvittering(brevStatusVOCaptor.getValue(), messageMock, kvitteringCaptor.getValue());

		KvitteringVO kvittering = kvitteringCaptor.getValue();
		BrevStatusVO brevStatus = brevStatusVOCaptor.getValue();

		assertThat(kvittering.getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_FERDIG));
		assertThat(brevStatus.getStatus(), is(Konstanter.BREVSTATUS_FERDIG));
	}

	@Test
	public void shouldOppdatereBrevStatusIfFeilFromDialogue() throws Exception {
		KvitteringVO kvittering = createDefaultKvittering();
		kvittering.setFeilniva("08");
		when(DialogueXMLParser.lagKvitteringVOFraDialogueMelding(any())).thenReturn(kvittering);
		when(brevserverServiceMock.hentBrevStatus(systemId, brevReferanse)).thenReturn(createDefaultBrevstatus());

		arkiverBrevCommand.execute();

		verify(brevserverServiceMock).lagreBrevStatus(brevstatusCaptor.capture(), any());
		verify(brevlagerServiceMock, never()).lagreBrev(any(KvitteringVO.class), any(Brevstatus.class), any(String.class));
		verify(messageProducerMock).sendKvittering(brevStatusVOCaptor.getValue(), messageMock, kvittering);

		BrevStatusVO brevStatus = brevStatusVOCaptor.getValue();

		assertThat(brevStatus.getStatus(), is(Konstanter.BREVSTATUS_FEIL));
	}

	@Test
	public void shouldOppdatereBrevStatusIfBrevetEksisterer() throws Exception {
		Brevstatus retBrevstatus = createDefaultBrevstatus();
		retBrevstatus.setStatus(Konstanter.BREVSTATUS_FERDIG);
		when(DialogueXMLParser.lagKvitteringVOFraDialogueMelding(any())).thenReturn(createDefaultKvittering());
		when(brevserverServiceMock.hentBrevStatus(systemId, brevReferanse)).thenReturn(retBrevstatus);

		arkiverBrevCommand.execute();

		verify(brevserverServiceMock, never()).lagreBrevStatus(any(Brevstatus.class), any(String.class));
		verify(brevlagerServiceMock, never()).lagreBrev(any(KvitteringVO.class), any(Brevstatus.class), any(String.class));
		verify(messageProducerMock).sendKvittering(brevStatusVOCaptor.capture(), any(MessageVO.class), kvitteringCaptor.capture());

		BrevStatusVO brevStatus = brevStatusVOCaptor.getValue();
		KvitteringVO kvittering = kvitteringCaptor.getValue();

		assertThat(brevStatus.getStatus(), is(Konstanter.BREVSTATUS_FEIL));
		assertThat(kvittering.getFeilkode(), is(Konstanter.FEIL_BREV_EKSISTERER));
	}

	private void mockConfigManager() {
		ConfigManager configManagerMock = mock(ConfigManager.class);
		mockStatic(ConfigManager.class);
		when(ConfigManager.getInstance()).thenReturn(configManagerMock);
		when(configManagerMock.getInt(ConfigManager.ARKIVER_HEADER_LENGDE, Konstanter.MELDING_HEADER_LENGTH)).thenReturn(
				Konstanter.MELDING_HEADER_LENGTH);
	}

	private KvitteringVO createDefaultKvittering() {
		KvitteringVO kvittering = new KvitteringVO();
		kvittering.setSystemID(systemId);
		kvittering.setBrevreferanse(brevReferanse);
		kvittering.setContentType(FilType.PDF.getContentType());
		kvittering.setFeilniva("0");
		return kvittering;
	}

	private Brevstatus createDefaultBrevstatus() {
		Brevstatus brevstatus = new Brevstatus();
		brevstatus.setStatus(Konstanter.BREVSTATUS_BREVPAKKE);
		return brevstatus;
	}

	private void setupBrevserverServiceMock() {
		mockStatic(BrevserverServiceFactory.class);
		BrevserverServiceFactory brevserverServiceFactoryMock = mock(BrevserverServiceFactory.class);
		when(BrevserverServiceFactory.getInstance()).thenReturn(brevserverServiceFactoryMock);
		when(brevserverServiceFactoryMock.createBrevserverService()).thenReturn(brevserverServiceMock);
	}

	private void setupBrevlagerMock() throws Exception {
		mockStatic(BrevlagerServiceFactory.class);
		BrevlagerServiceFactory brevlagerServiceFactoryMock = mock(BrevlagerServiceFactory.class);
		when(BrevlagerServiceFactory.getInstance()).thenReturn(brevlagerServiceFactoryMock);
		when(brevlagerServiceFactoryMock.createBrevlagerService()).thenReturn(brevlagerServiceMock);
	}

	private void setupMessageProducerMock() {
		mockStatic(MessageProducerFactory.class);
		MessageProducerFactory messageProducerFactoryMock = mock(MessageProducerFactory.class);
		when(MessageProducerFactory.getInstance()).thenReturn(messageProducerFactoryMock);
		when(messageProducerFactoryMock.createMessageProducer(SystemType.BI)).thenReturn(messageProducerMock);
	}

}
