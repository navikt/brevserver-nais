package no.nav.brevserver.service.jms;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.io.ByteArrayOutputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.jms.JMSAccessor;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit tests for MessageProducer abstract class
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PerformanceLogger.class, JMSAccessor.class })
@SuppressStaticInitializationFor({ "no.nav.brevserver.server.common.utility.PerformanceLogger" })
public class MessageProducerTest {

    private static final String REPLY_QUEUE = "queue://reply";
    private static final String SEND_QUEUE = "queue://send";
    private static final String SEND_QUEUE_JNDI = "queue/send";
    private static final byte[] BYTE_MSG = "What does the fox say?".getBytes();
    private static final String TEXT_MSG = "Rititititi!";
    private static final String CORRELATION_ID = "13";
    private static final String EXCEPTION_MSG = "MQ er ikke tilgjengelig";

    @Mock
    private JMSAccessor jmsAccessorMock;

    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    private MessageProducer messageProducer;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        setupJmsAccessorMock();
        messageProducer = createAnonymousMessageProducer();
    }

    @Test
    public void shouldSendByteMessageUsingQueueName() throws Exception {
        BytesMessage messageMock = mock(BytesMessage.class);
        when(jmsAccessorMock.createBytesMessage()).thenReturn(messageMock);

        messageProducer.sendByteMelding(BYTE_MSG, SEND_QUEUE, false);

        verifyStatic();
        JMSAccessor.getAccessorUsingQueueName(SEND_QUEUE);
        verify(messageMock).writeBytes(BYTE_MSG);
        verify(jmsAccessorMock).sendMessage(messageCaptor.capture(), eq(0L));
        assertThat((BytesMessage) messageCaptor.getValue(), is(messageMock));
        JMSAccessor.close(jmsAccessorMock);
    }

    @Test
    public void shouldSendByteMessageUsingJndiName() throws Exception {
        BytesMessage messageMock = mock(BytesMessage.class);
        when(jmsAccessorMock.createBytesMessage()).thenReturn(messageMock);

        messageProducer.sendByteMelding(BYTE_MSG, SEND_QUEUE_JNDI, true);

        verifyStatic();
        JMSAccessor.getAccessorUsingQueueJndiName(SEND_QUEUE_JNDI);
        verify(messageMock).writeBytes(BYTE_MSG);
        verify(jmsAccessorMock).sendMessage(messageCaptor.capture(), eq(0L));
        assertThat((BytesMessage) messageCaptor.getValue(), is(messageMock));
        JMSAccessor.close(jmsAccessorMock);
    }

    @Test
    public void shouldThrowExceptionIfWritingToMessageFails() throws Exception {
        thrown.expect(BrevTechnicalException.class);
        thrown.expectMessage(EXCEPTION_MSG);

        BytesMessage messageMock = mock(BytesMessage.class);
        when(jmsAccessorMock.createBytesMessage()).thenReturn(messageMock);
        doThrow(new JMSException("Fail!")).when(messageMock).writeBytes(BYTE_MSG);

        messageProducer.sendByteMelding(BYTE_MSG, SEND_QUEUE_JNDI, true);
    }

    @Test
    public void shouldSendRtfMessage() throws Exception {
        BytesMessage messageMock = mock(BytesMessage.class);
        when(jmsAccessorMock.createBytesMessage()).thenReturn(messageMock);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(BYTE_MSG);
        messageProducer.sendRTF(bos, SEND_QUEUE_JNDI, true);

        verifyStatic();
        JMSAccessor.getAccessorUsingQueueJndiName(SEND_QUEUE_JNDI);
        verify(messageMock).writeBytes(BYTE_MSG);
        verify(jmsAccessorMock).sendMessage(messageCaptor.capture(), eq(0L));
        assertThat((BytesMessage) messageCaptor.getValue(), is(messageMock));
        JMSAccessor.close(jmsAccessorMock);
    }

    @Test
    public void shouldProdusereTextMessageUsingQueueNameAndMessage() throws Exception {
        TextMessage messageMock = mock(TextMessage.class);

        messageProducer.produserTextMelding(SEND_QUEUE, false, messageMock, null, null);

        verifyStatic();
        JMSAccessor.getAccessorUsingQueueName(SEND_QUEUE);
        verify(jmsAccessorMock).sendMessage(messageCaptor.capture(), eq(0L));
        assertThat((TextMessage) messageCaptor.getValue(), is(messageMock));
        JMSAccessor.close(jmsAccessorMock);
    }

    @Test
    public void shouldProdusereTextMessageUsingJndiNameAndNoMessage() throws Exception {
        TextMessage messageMock = mock(TextMessage.class);
        when(jmsAccessorMock.createTextMessage(any(String.class))).thenReturn(messageMock);

        messageProducer.produserTextMelding(SEND_QUEUE_JNDI, true, null, CORRELATION_ID, TEXT_MSG);

        verifyStatic();
        JMSAccessor.getAccessorUsingQueueJndiName(SEND_QUEUE_JNDI);
        verify(messageMock).setJMSCorrelationID(CORRELATION_ID);
        verify(messageMock).setText(TEXT_MSG);
        verify(jmsAccessorMock).sendMessage(messageCaptor.capture(), eq(0L));
        assertThat((TextMessage) messageCaptor.getValue(), is(messageMock));
        JMSAccessor.close(jmsAccessorMock);
    }

    @Test
    public void shouldThrowExceptionWhenJmsOperationFails() throws Exception {
        thrown.expect(BrevTechnicalException.class);
        thrown.expectMessage(EXCEPTION_MSG);

        TextMessage messageMock = mock(TextMessage.class);
        when(jmsAccessorMock.createTextMessage(any(String.class))).thenReturn(messageMock);
        doThrow(new JMSException("Fail!")).when(messageMock).setText(TEXT_MSG);

        messageProducer.produserTextMelding(SEND_QUEUE_JNDI, true, null, CORRELATION_ID, TEXT_MSG);
    }

    @Test
    public void shouldSendToDeadLetterUsingByteMessage() throws Exception {
        Queue replyQueueMock = mock(Queue.class);
        JMSAccessor jmsAccessorMock2 = mock(JMSAccessor.class);
        BytesMessage bytesMessageMock = mock(BytesMessage.class);
        MessageVO messageMock = mock(MessageVO.class);

        when(jmsAccessorMock.createBytesMessage()).thenReturn(bytesMessageMock);
        when(messageMock.hasByteBody()).thenReturn(true);
        when(messageMock.getCorrelationID()).thenReturn(CORRELATION_ID);
        when(messageMock.getReplyQueueName()).thenReturn(REPLY_QUEUE);
        when(JMSAccessor.getAccessorUsingQueueName(REPLY_QUEUE)).thenReturn(jmsAccessorMock2);
        when(jmsAccessorMock2.getQueue()).thenReturn(replyQueueMock);

        messageProducer.sendToDeadLetter(messageMock, mock(PerformanceLogger.class), true, jmsAccessorMock);

        verifyStatic();
        JMSAccessor.getAccessorUsingQueueName(REPLY_QUEUE);
        verify(bytesMessageMock).setJMSReplyTo(replyQueueMock);
        verify(bytesMessageMock).setJMSCorrelationID(CORRELATION_ID);
        verify(jmsAccessorMock).sendMessage(bytesMessageMock, 0);
        JMSAccessor.close(jmsAccessorMock);
    }

    @Test
    public void shouldSendToDeadLetterUsingTextMessage() throws Exception {
        Queue replyQueueMock = mock(Queue.class);
        JMSAccessor jmsAccessorMock2 = mock(JMSAccessor.class);
        TextMessage textMessageMock = mock(TextMessage.class);
        MessageVO messageMock = mock(MessageVO.class);

        when(jmsAccessorMock.createTextMessage(any(String.class))).thenReturn(textMessageMock);
        when(messageMock.hasByteBody()).thenReturn(false);
        when(messageMock.getCorrelationID()).thenReturn(CORRELATION_ID);
        when(messageMock.getStringBody()).thenReturn(TEXT_MSG);
        when(messageMock.getReplyQueueName()).thenReturn(REPLY_QUEUE);
        when(JMSAccessor.getAccessorUsingQueueName(REPLY_QUEUE)).thenReturn(jmsAccessorMock2);
        when(jmsAccessorMock2.getQueue()).thenReturn(replyQueueMock);

        messageProducer.sendToDeadLetter(messageMock, mock(PerformanceLogger.class), true, jmsAccessorMock);

        verifyStatic();
        JMSAccessor.getAccessorUsingQueueName(REPLY_QUEUE);
        verify(textMessageMock).setText(TEXT_MSG);
        verify(textMessageMock).setJMSReplyTo(replyQueueMock);
        verify(textMessageMock).setJMSCorrelationID(CORRELATION_ID);
        verify(jmsAccessorMock).sendMessage(textMessageMock, 0);
        JMSAccessor.close(jmsAccessorMock);
    }

    @Test
    public void shouldThrowExceptionWhenSendDeadLetterIfJmsOperationFails() throws Exception {
        thrown.expect(BrevTechnicalException.class);
        thrown.expectMessage(EXCEPTION_MSG);

        MessageVO messageMock = mock(MessageVO.class);
        TextMessage textMessageMock = mock(TextMessage.class);
        when(jmsAccessorMock.createTextMessage(any(String.class))).thenReturn(textMessageMock);
        when(messageMock.hasByteBody()).thenReturn(false);
        when(messageMock.getCorrelationID()).thenReturn(CORRELATION_ID);
        doThrow(new JMSException("FAIL!")).when(textMessageMock).setJMSCorrelationID(CORRELATION_ID);

        messageProducer.sendToDeadLetter(messageMock, mock(PerformanceLogger.class), true, jmsAccessorMock);
    }

    private void setupJmsAccessorMock() throws Exception {
        mockStatic(JMSAccessor.class);
        when(JMSAccessor.getAccessorUsingQueueName(SEND_QUEUE)).thenReturn(jmsAccessorMock);
        when(JMSAccessor.getAccessorUsingQueueJndiName(SEND_QUEUE_JNDI)).thenReturn(jmsAccessorMock);
    }

    private MessageProducer createAnonymousMessageProducer() {
        return new MessageProducer() {
            {
                log = mock(Log.class);
            }

            @Override
            public void sendToDialogue(MessageVO message) throws BrevTechnicalException {

            }

            @Override
            public void sendReturMelding(String queueName, boolean useJndi, String correlationID, String kvittering)
                    throws BrevTechnicalException {
            }

            @Override
            public void sendKvittering(BrevStatusVO brevStatusVo, MessageVO messageVo, KvitteringVO kvittering)
                    throws BrevTechnicalException {
            }

            @Override
            public void deadLetter(MessageVO msgVO) throws BrevTechnicalException {
            }
        };
    }
}
