package no.nav.brevserver.server.common.jms;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;

import no.nav.brevserver.server.common.cache.CacheManager;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.jndi.JndiHelper;
import no.nav.brevserver.server.common.utility.PerformanceLogger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PerformanceLogger.class, JndiHelper.class, CacheManager.class, ConfigManager.class })
@SuppressStaticInitializationFor({ "no.nav.brevserver.server.common.utility.PerformanceLogger" })
public class JMSAccessorTest {

    private static final String TEST_QUEUE = "queue://test/queue";
    private static final String TEST_QUEUE_JNDI = "test/queue";

    private static final String MESSAGE_ID = "10000";

    @Mock
    private ConfigManager configManagerMock;
    @Mock
    private JndiHelper jndiHelperMock;
    @Mock
    private Queue queueMock;
    @Mock
    private QueueSession queueSessionMock;
    @Mock
    private QueueConnection queueConnectionMock;
    @Mock
    private QueueConnectionFactory queueConnectionFactoryMock;

    private JMSAccessor jmsAccessor;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setUpConfigManagerMock();
        setupJndiHelperMock();
        mockStatic(CacheManager.class);
    }

    @Test
    public void shouldReturnNullIfJndiNameIsNullOrBlank() throws Exception {
        assertThat(JMSAccessor.getAccessorUsingQueueJndiName(null), nullValue());
        assertThat(JMSAccessor.getAccessorUsingQueueJndiName(""), nullValue());
    }

    @Test
    public void shouldReturnNullIfQueueNameIsNullOrBlank() throws Exception {
        assertThat(JMSAccessor.getAccessorUsingQueueName(null), nullValue());
        assertThat(JMSAccessor.getAccessorUsingQueueName(""), nullValue());
    }

    @Test
    public void shouldCreateJmsAccessorFromJndiName() throws Exception {
        jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(TEST_QUEUE_JNDI);

        assertThat(jmsAccessor, instanceOf(JMSAccessor.class));
        assertThat(jmsAccessor.getQueue(), is(queueMock));
        assertThat(jmsAccessor.isOpen(), is(true));
    }

    @Test
    public void shouldCreateJmsAccessorFromQueueName() throws Exception {
        jmsAccessor = JMSAccessor.getAccessorUsingQueueName(TEST_QUEUE);

        assertThat(jmsAccessor, instanceOf(JMSAccessor.class));
        assertThat(jmsAccessor.getQueue(), is(queueMock));
        assertThat(jmsAccessor.isOpen(), is(true));
    }

    @Test
    public void shouldGetQueueBrowser() throws Exception {
        QueueBrowser queueBrowserMock = mock(QueueBrowser.class);
        when(queueSessionMock.createBrowser(queueMock)).thenReturn(queueBrowserMock);
        jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(TEST_QUEUE_JNDI);

        assertThat(jmsAccessor.getBrowser(), is(queueBrowserMock));
    }

    @Test
    public void shouldThrowExceptionIfQueueBrowserCouldNotBeCreated() throws Exception {
        thrown.expect(BrevTechnicalException.class);
        thrown.expectMessage("MQ er ikke tilgjengelig");

        when(queueSessionMock.createBrowser(queueMock)).thenThrow(new JMSException("Nei!"));
        jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(TEST_QUEUE_JNDI);

        jmsAccessor.getBrowser();
    }

    @Test
    public void shouldVerifyThatListenerIsRunningAndThereAreNoMessagesInQueue() throws Exception {
        QueueBrowser queueBrowserMock = mock(QueueBrowser.class);
        Enumeration<?> enumerationMock = mock(Enumeration.class);
        when(queueSessionMock.createBrowser(queueMock)).thenReturn(queueBrowserMock);
        when(queueBrowserMock.getEnumeration()).thenReturn(enumerationMock);
        jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(TEST_QUEUE_JNDI);

        boolean listenerRunning = jmsAccessor.isListenerRunning();

        assertThat(listenerRunning, is(true));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldVerifyThatListenerIsRunningAndThereAreMessagesInQueue() throws Exception {
        QueueBrowser queueBrowserMock = mock(QueueBrowser.class);
        Enumeration<Object> enumerationMock = mock(Enumeration.class);
        Message messageMock = mock(Message.class);

        when(queueMock.getQueueName()).thenReturn(TEST_QUEUE);
        when(messageMock.getJMSMessageID()).thenReturn(MESSAGE_ID);
        when(enumerationMock.hasMoreElements()).thenReturn(true);
        when(enumerationMock.nextElement()).thenReturn((Object) messageMock);
        when(queueSessionMock.createBrowser(queueMock)).thenReturn(queueBrowserMock);
        when(queueBrowserMock.getEnumeration()).thenReturn(enumerationMock);
        jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(TEST_QUEUE_JNDI);

        boolean listenerRunning = jmsAccessor.isListenerRunning();

        verifyStatic();
        String cacheId = "JMSAccessor." + TEST_QUEUE + ".id";
        CacheManager.getObject(cacheId);
        CacheManager.addObject(cacheId, MESSAGE_ID);

        assertThat(listenerRunning, is(true));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldVerifyThatListenerIsRunningAndThereAreMessagesInQueueAndNoMessageInCache() throws Exception {
        QueueBrowser queueBrowserMock = mock(QueueBrowser.class);
        Enumeration<Object> enumerationMock = mock(Enumeration.class);
        Message messageMock = mock(Message.class);

        when(queueMock.getQueueName()).thenReturn(TEST_QUEUE);
        when(messageMock.getJMSMessageID()).thenReturn(MESSAGE_ID);
        when(enumerationMock.hasMoreElements()).thenReturn(true);
        when(enumerationMock.nextElement()).thenReturn((Object) messageMock);
        when(queueSessionMock.createBrowser(queueMock)).thenReturn(queueBrowserMock);
        when(queueBrowserMock.getEnumeration()).thenReturn(enumerationMock);
        when(configManagerMock.getInt("JMSAccessor.isListenerRunning.okAgeInMin", 3)).thenReturn(3);
        String cacheId = "JMSAccessor." + TEST_QUEUE + ".id";
        when(CacheManager.getObject(cacheId)).thenReturn(MESSAGE_ID);
        when(CacheManager.getAge(cacheId)).thenReturn(240000L);

        jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(TEST_QUEUE_JNDI);

        boolean listenerRunning = jmsAccessor.isListenerRunning();

        verifyStatic();
        CacheManager.getObject(cacheId);

        assertThat(listenerRunning, is(false));
    }

    @Test
    public void shouldGetQueueNameFromDestination() throws Exception {
        when(queueMock.getQueueName()).thenReturn("queue://MRT1/QA.T343.ASDFASDF_ASDF_ASDF_SADF?CCSID=1&SG=23");
        jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(TEST_QUEUE_JNDI);

        assertThat(jmsAccessor.getQueueName(), is("QA.T343.ASDFASDF_ASDF_ASDF_SADF"));
    }

    @Test
    public void shouldCloseJmsAccessorResources() throws Exception {
        jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(TEST_QUEUE_JNDI);

        jmsAccessor.close();

        verify(queueSessionMock).close();
        verify(queueConnectionMock).close();
    }

    @Test
    public void shouldCloseJmsAccessorResourcesUsingStaticMethod() throws Exception {
        jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(TEST_QUEUE_JNDI);

        JMSAccessor.close(jmsAccessor);

        verify(queueSessionMock).close();
        verify(queueConnectionMock).close();
    }

    private void setUpConfigManagerMock() {
        mockStatic(ConfigManager.class);
        when(ConfigManager.getInstance()).thenReturn(configManagerMock);
    }

    private void setupJndiHelperMock() throws Exception {
        mockStatic(JndiHelper.class);

        when(queueSessionMock.createQueue(any(String.class))).thenReturn(queueMock);
        when(queueConnectionMock.createQueueSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(queueSessionMock);
        when(queueConnectionFactoryMock.createQueueConnection()).thenReturn(queueConnectionMock);
        when(JndiHelper.getInstance()).thenReturn(jndiHelperMock);
        when(jndiHelperMock.lookup(Queue.class, TEST_QUEUE_JNDI)).thenReturn(queueMock);
        when(jndiHelperMock.lookup(eq(QueueConnectionFactory.class), any(String.class))).thenReturn(
                queueConnectionFactoryMock);
    }

}
