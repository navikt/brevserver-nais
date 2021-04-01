package no.nav.brevserver.mdb;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import javax.jms.JMSException;
import javax.jms.Message;

import no.nav.brevserver.command.AbstractCommand;
import no.nav.brevserver.command.CommandFactory;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.type.QueueType;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.jms.MessageProducer;
import no.nav.brevserver.service.jms.MessageProducerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PerformanceLogger.class, MessageProducerFactory.class, MessageVO.class, CommandFactory.class,
		BiAbstractMdb.class, PeAbstractMdb.class })
@SuppressStaticInitializationFor({ "no.nav.brevserver.server.common.utility.PerformanceLogger" })
public class BiAbstractMdbTest {

	private static final QueueType QUEUE_TYPE = QueueType.BI_BREVSERVER_ONLINEBREV;
	private static final SystemType SYSTEM_TYPE = SystemType.BI;
	private static final BrevTechnicalException eCritical = new BrevTechnicalException(
			BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, "Databasefeil");
	private static final BrevTechnicalException eDead = new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, "Xmlfeil");
	private static final BrevException eOther = new BrevException("");

	private AbstractMdb biAbstractMdb;

	@Mock
	private MessageProducer messageProducerMock;
	@Mock
	private Message messageMock;
	@Mock
	private MessageVO messageVOMock;
	@Mock
	private AbstractCommand abstractCommandMock;
	@Mock
	private Log logMock;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockStatic(PerformanceLogger.class);
		AbstractMdbTestUtility.mockMessageProducer(SYSTEM_TYPE, messageProducerMock);
		AbstractMdbTestUtility.mockMessageVO(messageMock, messageVOMock);
		AbstractMdbTestUtility.mockCommandFactory(QUEUE_TYPE, messageVOMock, abstractCommandMock);
		biAbstractMdb = new BiAbstractMdb() {
		};
		biAbstractMdb.log = logMock;
	}

	@Test
	public void shouldProcessMessageCorrectly() throws Exception {
		biAbstractMdb.onMessage(null, messageMock, QUEUE_TYPE);
		verify(abstractCommandMock).execute();
		verify(messageProducerMock, never()).deadLetter(messageVOMock);
		verify(messageMock).acknowledge();
	}

	@Test
	public void shouldThrowRuntimeExceptionIfSpecificError() throws Exception {
		doThrow(eCritical).when(abstractCommandMock).execute();
		try {
			biAbstractMdb.onMessage(null, messageMock, QUEUE_TYPE);
			fail("Should not get here");
		} catch (RuntimeException e) {
			verify(messageMock, never()).acknowledge();
		}
	}

	@Test
	public void shouldSendMessageToDeadletterIfSpecificError() throws Exception {
		doThrow(eDead).when(abstractCommandMock).execute();
		biAbstractMdb.onMessage(null, messageMock, QUEUE_TYPE);
		verify(messageProducerMock).deadLetter(messageVOMock);
		verify(messageMock).acknowledge();
	}

	@Test
	public void shouldIgnoreIfOtherError() throws Exception {
		doThrow(eOther).when(abstractCommandMock).execute();
		biAbstractMdb.onMessage(null, messageMock, QUEUE_TYPE);
		verify(messageProducerMock, never()).deadLetter(messageVOMock);
		verify(messageMock).acknowledge();
	}

	@Test
	public void shouldThrowRuntimeExceptionIfAckFailes() throws Exception {
		doThrow(new JMSException("")).when(messageMock).acknowledge();
		try {
			biAbstractMdb.onMessage(null, messageMock, QUEUE_TYPE);
			fail("Should not get here");
		} catch (RuntimeException e) {
			BrevException be = (BrevException) e.getCause();
			assertTrue(be.isFeilkode(BrevTechnicalException.MQ_IKKE_TILGJENGELIG));
		}
	}
}
