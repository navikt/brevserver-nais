package no.nav.brevserver.mdb;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import javax.jms.Message;

import no.nav.brevserver.command.AbstractCommand;
import no.nav.brevserver.command.CommandFactory;
import no.nav.brevserver.server.common.type.QueueType;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.jms.MessageProducer;
import no.nav.brevserver.service.jms.MessageProducerFactory;
import no.nav.brevserver.service.xml.XMLService;
import no.nav.brevserver.service.xml.XMLServiceFactory;

public class AbstractMdbTestUtility {
	static void mockMessageProducer(SystemType systemType, MessageProducer messageProducerMock) {
		mockStatic(MessageProducerFactory.class);
		MessageProducerFactory messageProducerFactoryMock = mock(MessageProducerFactory.class);
		when(MessageProducerFactory.getInstance()).thenReturn(messageProducerFactoryMock);
		when(messageProducerFactoryMock.createMessageProducer(systemType)).thenReturn(messageProducerMock);
	}

	static void mockMessageVO(Message messageMock, MessageVO messageVOMock) throws Exception {
		// NB!! Klassen som kaller konstruktøren må også legges til i @PrepareForTest
		mockStatic(MessageVO.class);
		whenNew(MessageVO.class).withParameterTypes(Message.class).withArguments(messageMock)
				.thenReturn(messageVOMock);
	}

	static void mockCommandFactory(QueueType queueType, MessageVO messageVOMock, AbstractCommand abstractCommandMock) {
		mockStatic(CommandFactory.class);
		CommandFactory commandFactoryMock = mock(CommandFactory.class);
		when(CommandFactory.getInstance()).thenReturn(commandFactoryMock);
		when(commandFactoryMock.createCommand(queueType, messageVOMock)).thenReturn(abstractCommandMock);
	}

	static void mockKvitteringVO(KvitteringVO kvitteringVOMock) throws Exception {
		// NB!! Klassen som kaller konstruktøren må også legges til i @PrepareForTest
		mockStatic(KvitteringVO.class);
		whenNew(KvitteringVO.class).withNoArguments().thenReturn(kvitteringVOMock);
	}
	
	static void mockXMLServiceFactory(XMLService xmlServiceMock) {
		mockStatic(XMLServiceFactory.class);
		XMLServiceFactory xmlServiceFactoryMock = mock(XMLServiceFactory.class);
		when(XMLServiceFactory.getInstance()).thenReturn(xmlServiceFactoryMock);
		when(xmlServiceFactoryMock.createXMLService()).thenReturn(xmlServiceMock);
	}
}
