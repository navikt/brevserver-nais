package no.nav.brevserver.command;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.StringReader;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import no.nav.brevserver.service.jms.MessageProducer;
import no.nav.brevserver.service.jms.MessageProducerFactory;
import no.nav.brevserver.service.xml.XMLService;
import no.nav.brevserver.service.xml.XMLServiceFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

/**
 * Unit tests for BestillBrevCommand
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfigManager.class, BrevserverServiceFactory.class, XMLServiceFactory.class, 
		MessageProducerFactory.class, DialogueXMLParser.class })
public class BestillBrevCommandTest {

	@Mock
	private XMLService xmlServiceMock;
	@Mock
	private BrevserverService brevserverServiceMock;
	@Mock
	private MessageProducer messageProducerMock;
	@Mock
	private MessageVO messageMock; 
	@Captor
	private ArgumentCaptor<BrevStatusVO> brevStatusCaptor;
	@Captor
	private ArgumentCaptor<KvitteringVO> kvitteringCaptor;
	@Captor
	private ArgumentCaptor<String> stringCaptor;
	
	private final String brevreferanse = "12345";
	private final String systemId = "BI12";
	private final String passord = "helloworld!";
	private final String replyQueue = "ReplyQueue";
	private final String correlationId = "Correlation!";
	private final String stringBody = "<dummyxml>Derp!</dummyxml>";
	private final String xmlKvitteringIngenTilgang = "<kvittering>ingentilgang!</kvittering>";
	private final String xmlKvitteringBrevEksisterer = "<kvittering>eksisterer!</kvittering>";
	private final String token = "MyLittleToken";
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private BestillBrevCommand bestillBrevCommand;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		setupMockMessage();
		mockConfigManager();
		setupXmlServiceMock();
		setupBrevserverServiceMock();
		setupMessageProducerMock();
		
		bestillBrevCommand = new BestillBrevCommand(messageMock);
	}

	@Test
	public void shouldThrowExceptionIfThereIsNoMessageXml() throws Exception {
		expectedException.expect(BrevTechnicalException.class);
		expectedException.expectMessage("XML fail");
		
		when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenThrow(new BrevTechnicalException("XML fail"));
		
		Whitebox.<Void> invokeMethod(bestillBrevCommand, "validate");
	}
	
	@Test
	public void shouldThrowExceptionIfSystemIdIsNotBI() throws Exception {
		expectedException.expect(BrevTechnicalException.class);
		expectedException.expectMessage("Brev med feil systemID mottatt: 'PE2', forventet ikke pensjonsbrev");
		
		BrevStatusVO brevstatus = createDefaultBrevstatus();
		brevstatus.setSystemID("PE2");
		when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenReturn(brevstatus);
		
		Whitebox.<Void> invokeMethod(bestillBrevCommand, "validate");
	}
		
	@Test
	public void shouldThrowExceptionIfABrevstatusFieldIsMissing() throws Exception {
		expectedException.expect(BrevTechnicalException.class);
		
		when(messageMock.getReplyQueueName()).thenReturn("");
		
		when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenReturn(createDefaultBrevstatus());
		
		Whitebox.<Void> invokeMethod(bestillBrevCommand, "validate");
	}
	
	@Test
	public void shouldPassValidation() throws Exception {
		when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenReturn(createDefaultBrevstatus());
		
		Whitebox.<Void> invokeMethod(bestillBrevCommand, "validate");
		
		assertThat(bestillBrevCommand.brevStatusVo.getBrevreferanse(), is(brevreferanse));
		assertThat(bestillBrevCommand.brevStatusVo.getSystemID(), is(systemId));
		assertThat(bestillBrevCommand.brevStatusVo.getReturKoe(), is(replyQueue));
	}

	@Test
	public void shouldBestilleBrevFraDialogue() throws Exception {
		when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenReturn(createDefaultBrevstatus());
		when(brevserverServiceMock.sjekkSystemTilgang(systemId, passord)).thenReturn(true);
		when(brevserverServiceMock.hentBrevStatus(systemId, brevreferanse)).thenReturn(null);
		
		bestillBrevCommand.execute();
		
		verify(brevserverServiceMock).lagreBrevStatus(brevStatusCaptor.capture());
		verify(messageProducerMock).sendToDialogue(any(MessageVO.class));
		
		BrevStatusVO brevstatus = brevStatusCaptor.getValue();
		assertThat(brevstatus.getStatus(), is(Konstanter.BREVSTATUS_BREVPAKKE));
		assertThat(brevstatus.getSystemID(), is(systemId));
		assertThat(brevstatus.getBrevreferanse(), is(brevreferanse));
		assertThat(brevstatus.getReturKoe(), is(replyQueue));
	}
	
	@Test
	public void shouldSendReturFeilmeldingIfNoSystemTilgang() throws Exception {
		when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenReturn(createDefaultBrevstatus());
		when(brevserverServiceMock.sjekkSystemTilgang(systemId, passord)).thenReturn(false);
		when(xmlServiceMock.unmarshal(any(KvitteringVO.class), any(BrevStatusVO.class))).thenReturn(xmlKvitteringIngenTilgang);
		
		bestillBrevCommand.execute();
		
		verify(messageProducerMock).sendReturMelding(eq(replyQueue), eq(false), eq(correlationId), stringCaptor.capture());
		
		assertThat(stringCaptor.getValue(), is(xmlKvitteringIngenTilgang));
	}
	
	@Test
	public void shouldGiveBrevtilgangToFagsystemWithToken() throws Exception {
		BrevStatusVO brevstatus = createDefaultBrevstatus();
		brevstatus.setModus(Konstanter.BREVMODUS_FRALAGER);
		brevstatus.setToken(token);
		when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenReturn(brevstatus);
		when(brevserverServiceMock.sjekkSystemTilgang(systemId, passord)).thenReturn(true);
		
		bestillBrevCommand.execute();
		
		verify(brevserverServiceMock).lagreTilgang(systemId, brevreferanse, token);
	}
	
	@Test
	public void shouldGiveBrevtilgangToElinWithToken() throws Exception {
		String systemIdElin = "OB05";
		BrevStatusVO brevstatus = createDefaultBrevstatus();
		brevstatus.setSystemID(systemIdElin);
		brevstatus.setModus(Konstanter.BREVMODUS_FRALAGER);
		brevstatus.setToken(token);
		when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenReturn(brevstatus);
		when(brevserverServiceMock.sjekkSystemTilgang(systemIdElin, passord)).thenReturn(true);
		
		bestillBrevCommand.execute();
		
		verify(brevserverServiceMock).lagreTilgang(systemIdElin, brevreferanse, token);
	}
	
	@Test
	public void shouldSendReturmeldingIfBrevetEksisterer() throws Exception {
		when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenReturn(createDefaultBrevstatus());
		when(brevserverServiceMock.sjekkSystemTilgang(systemId, passord)).thenReturn(true);
		when(brevserverServiceMock.hentBrevStatus(systemId, brevreferanse)).thenReturn(new BrevStatusVO());
		when(xmlServiceMock.unmarshal(any(KvitteringVO.class), any(BrevStatusVO.class))).thenReturn(xmlKvitteringBrevEksisterer);
		
		bestillBrevCommand.execute();
		
		verify(messageProducerMock).sendReturMelding(eq(replyQueue), eq(false), eq(correlationId), stringCaptor.capture());
		
		assertThat(stringCaptor.getValue(), is(xmlKvitteringBrevEksisterer));
	}
	
	private void mockConfigManager() {
		ConfigManager configManagerMock = mock(ConfigManager.class);
		mockStatic(ConfigManager.class);
		when(ConfigManager.getInstance()).thenReturn(configManagerMock);
		when(configManagerMock.getInt(ConfigManager.ARKIVER_HEADER_LENGDE, Konstanter.MELDING_HEADER_LENGTH)).thenReturn(
				Konstanter.MELDING_HEADER_LENGTH);
	}
	
	private BrevStatusVO createDefaultBrevstatus() {
		BrevStatusVO brevstatus = new BrevStatusVO();
		brevstatus.setSystemID(systemId);
		brevstatus.setBrevreferanse(brevreferanse);
		brevstatus.setPassord(passord);
		return brevstatus;
	}

	private void setupMockMessage() {
		when(messageMock.getStringBody()).thenReturn(stringBody);
		when(messageMock.getReplyQueueName()).thenReturn(replyQueue);
		when(messageMock.getCorrelationID()).thenReturn(correlationId);
	}
	
	private void setupXmlServiceMock() throws Exception {
		mockStatic(XMLServiceFactory.class);
		XMLServiceFactory xmlServiceFactoryMock = mock(XMLServiceFactory.class);
		when(XMLServiceFactory.getInstance()).thenReturn(xmlServiceFactoryMock);
		when(xmlServiceFactoryMock.createXMLService()).thenReturn(xmlServiceMock);
	}
	
	private void setupBrevserverServiceMock() {
		mockStatic(BrevserverServiceFactory.class);
		BrevserverServiceFactory brevserverServiceFactoryMock = mock(BrevserverServiceFactory.class);
		when(BrevserverServiceFactory.getInstance()).thenReturn(brevserverServiceFactoryMock);
		when(brevserverServiceFactoryMock.createBrevserverService()).thenReturn(brevserverServiceMock);
	}
	
	private void setupMessageProducerMock() {
		mockStatic(MessageProducerFactory.class);
		MessageProducerFactory messageProducerFactoryMock = mock(MessageProducerFactory.class);
		when(MessageProducerFactory.getInstance()).thenReturn(messageProducerFactoryMock);
		when(messageProducerFactoryMock.createMessageProducer(SystemType.BI)).thenReturn(messageProducerMock);
	}
	
}
