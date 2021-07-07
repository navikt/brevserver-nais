package no.nav.brevserver.command;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.StringReader;

import no.nav.brevserver.core.domain.entities.Brevstatus;
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
 * Unit tests for PEBestillBrevCommand
 * 
 * @author Joakim Bj�rnstad, Visma Consulting
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfigManager.class, BrevserverServiceFactory.class, XMLServiceFactory.class, 
        MessageProducerFactory.class})
public class PEBestillBrevCommandTest {

    private static final String BREVREFERANSE = "12345";
    private static final String SYSTEM_ID = "PE00";
    private static final String PASSORD = "helloworld!";
    private static final String REPLY_QUEUE = "ReplyQueue";
    private static final String STRING_BODY = "StringBody";
    private static final String CORRELATION_ID = "122333";
    private static final String KVITTERING_XML_EXISTS = "<kvittering>eksisterer!</kvittering>";
    private static final String TOKEN = "Token!";
    
    @Mock
    private XMLService xmlServiceMock;
    @Mock
    private BrevserverService brevserverServiceMock;
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
    @Captor
    private ArgumentCaptor<String> stringCaptor;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    private PEBestillBrevCommand bestillBrevCommand;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockConfigManager();
        setupXmlServiceMock();
        setupBrevserverServiceMock();
        setupMessageProducerMock();
        setupMockMessage();
        
        bestillBrevCommand = new PEBestillBrevCommand(messageMock);
    }
    
    @Test
    public void shouldThrowExceptionIfThereIsNoMessageXml() throws Exception {
        expectedException.expect(BrevTechnicalException.class);
        expectedException.expectMessage("XML fail");
        
        when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenThrow(new BrevTechnicalException("XML fail"));
        
        Whitebox.<Void> invokeMethod(bestillBrevCommand, "validate");
    }
    
    @Test
    public void shouldThrowExceptionIfSystemIdIsNotPE() throws Exception {
        expectedException.expect(BrevTechnicalException.class);
        expectedException.expectMessage("Brev med feil systemID mottatt: 'BI00', forventet prefix: '"+ SystemType.PE + "'");
        
        BrevStatusVO brevstatus = createDefaultBrevstatus();
        brevstatus.setSystemID("BI00");
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
        
        BrevStatusVO brevStatus = (BrevStatusVO) bestillBrevCommand.getResult();
        assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
        assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
        assertThat(brevStatus.getReturKoe(), is(REPLY_QUEUE));
    }
    
    @Test
    public void shouldGiveBrevtilgangToFagsystemWithToken() throws Exception {
        when(messageMock.isTilgangsXML()).thenReturn(true);
        BrevStatusVO brevstatus = createDefaultBrevstatus();
        brevstatus.setToken(TOKEN);
        when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenReturn(brevstatus);
        when(brevserverServiceMock.sjekkSystemTilgang(SYSTEM_ID, PASSORD)).thenReturn(true);
        
        bestillBrevCommand.execute();
        
        verify(brevserverServiceMock).lagreTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);
    }
    
    @Test
    public void shouldBestilleBrevFraDialogueIfBrevDoesNotExist() throws Exception {
        when(messageMock.isTilgangsXML()).thenReturn(false);
        when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenReturn(createDefaultBrevstatus());
        when(brevserverServiceMock.hentBrevStatus(SYSTEM_ID, BREVREFERANSE)).thenReturn(null);
        
        bestillBrevCommand.execute();
        
        verify(brevserverServiceMock).lagreBrevStatus(brevstatusCaptor.capture(), any());
        verify(messageProducerMock).sendToDialogue(any(MessageVO.class));
        
        assertThat(brevStatusVOCaptor.getValue().getStatus(), is(Konstanter.BREVSTATUS_BREVPAKKE));
    }
    
    @Test
    public void shouldBestilleBrevFraDialogueIfBrevDoesExist() throws Exception {
        when(messageMock.isTilgangsXML()).thenReturn(false);
        when(xmlServiceMock.marshalBrevStatus(any(StringReader.class))).thenReturn(createDefaultBrevstatus());
        when(brevserverServiceMock.hentBrevStatus(SYSTEM_ID, BREVREFERANSE)).thenReturn(new Brevstatus());
        when(xmlServiceMock.unmarshal(any(KvitteringVO.class), any(BrevStatusVO.class))).thenReturn(KVITTERING_XML_EXISTS);

        bestillBrevCommand.execute();
        
        verify(messageProducerMock, never()).sendToDialogue(any(MessageVO.class));
        verify(messageProducerMock).sendReturMelding(eq(REPLY_QUEUE), eq(false), eq(CORRELATION_ID), stringCaptor.capture());
        
        assertThat(stringCaptor.getValue(), is(KVITTERING_XML_EXISTS));
    }
    
    private BrevStatusVO createDefaultBrevstatus() {
        BrevStatusVO brevstatus = new BrevStatusVO();
        brevstatus.setSystemID(SYSTEM_ID);
        brevstatus.setBrevreferanse(BREVREFERANSE);
        brevstatus.setPassord(PASSORD);
        return brevstatus;
    }
    
    private void setupMockMessage() {
        when(messageMock.getStringBody()).thenReturn(STRING_BODY);
        when(messageMock.getReplyQueueName()).thenReturn(REPLY_QUEUE);
        when(messageMock.getCorrelationID()).thenReturn(CORRELATION_ID);
    }
    
    private void mockConfigManager() {
        ConfigManager configManagerMock = mock(ConfigManager.class);
        mockStatic(ConfigManager.class);
        when(ConfigManager.getInstance()).thenReturn(configManagerMock);
        when(configManagerMock.getInt(ConfigManager.ARKIVER_HEADER_LENGDE, Konstanter.MELDING_HEADER_LENGTH)).thenReturn(
                Konstanter.MELDING_HEADER_LENGTH);
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
        when(messageProducerFactoryMock.createMessageProducer(SystemType.PE)).thenReturn(messageProducerMock);
    }
}
