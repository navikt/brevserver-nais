package no.nav.brevserver.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import no.nav.brevserver.consumer.joark.JoarkServiceBi;
import no.nav.brevserver.consumer.joark.factory.JoarkServiceBeanFactory;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.KnappStatus;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.service.brevlager.BrevlagerService;
import no.nav.brevserver.service.brevlager.BrevlagerServiceFactory;
import no.nav.brevserver.service.brevlager.beans.BrevlagerServiceBean;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import no.nav.brevserver.service.brevserver.beans.BrevserverServiceBean;
import no.nav.brevserver.service.converter.FileConverter;
import no.nav.brevserver.service.jms.MessageProducer;
import no.nav.brevserver.service.jms.MessageProducerFactory;
import no.nav.brevserver.service.xml.XMLService;
import no.nav.brevserver.service.xml.XMLServiceFactory;

public class ControllerBeanDelegateTestUtility {

	static BrevVO mockBrevVO(String systemId, String lagerStatus, String contentType, byte[] brevData)
			throws BrevTechnicalException {
		BrevVO brevVOMock = mock(BrevVO.class);
		when(brevVOMock.getSystemID()).thenReturn(systemId);
		when(brevVOMock.getLagerStatus()).thenReturn(lagerStatus);
		when(brevVOMock.getContentType()).thenReturn(contentType);
		when(brevVOMock.getBrevdata()).thenReturn(brevData);
		return brevVOMock;
	}

	static BrevStatusVO mockBrevStatusVO(String systemId, String brevreferanse, String token, String status) {
		BrevStatusVO brevstStatusVOMock = mock(BrevStatusVO.class);
		when(brevstStatusVOMock.getSystemID()).thenReturn(systemId);
		when(brevstStatusVOMock.getBrevreferanse()).thenReturn(brevreferanse);
		when(brevstStatusVOMock.getToken()).thenReturn(token);
		when(brevstStatusVOMock.getStatus()).thenReturn(status);
		when(brevstStatusVOMock.getKnappStatus()).thenReturn(new KnappStatus(KnappStatus.LAGRE_KLADD));
		return brevstStatusVOMock;
	}
	
	static Log mockLog() throws Exception {
		mockStatic(Log.class);
		Log logMock = mock(Log.class);
		whenNew(Log.class).withParameterTypes(Class.class).withArguments(any()).thenReturn(logMock);
		return logMock;
	}

	static ConfigManager mockConfigManager() {
		mockStatic(ConfigManager.class);
		ConfigManager configManagerMock = mock(ConfigManager.class);
		when(ConfigManager.getInstance()).thenReturn(configManagerMock);
		return configManagerMock;
	}

	static PerformanceLogger mockPerformanceLogger() {
		mockStatic(PerformanceLogger.class);
		PerformanceLogger performanceLoggerMock = mock(PerformanceLogger.class);
		return performanceLoggerMock;
	}

	static BrevserverService mockBrevserverService() throws BrevTechnicalException {
		mockStatic(BrevserverServiceFactory.class);
		BrevserverServiceFactory brevserverServiceFactoryMock = mock(BrevserverServiceFactory.class);
		when(BrevserverServiceFactory.getInstance()).thenReturn(brevserverServiceFactoryMock);
		BrevserverServiceBean brevserverServiceBeanMock = mock(BrevserverServiceBean.class);
		when(brevserverServiceFactoryMock.createBrevserverService()).thenReturn(brevserverServiceBeanMock);
		return brevserverServiceBeanMock;
	}

	static BrevlagerService mockBrevlagerService() {
		mockStatic(BrevlagerServiceFactory.class);
		BrevlagerServiceFactory brevlagerServiceFactoryMock = mock(BrevlagerServiceFactory.class);
		when(BrevlagerServiceFactory.getInstance()).thenReturn(brevlagerServiceFactoryMock);
		BrevlagerServiceBean brevlagerServiceBeanMock = mock(BrevlagerServiceBean.class);
		when(brevlagerServiceFactoryMock.createBrevlagerService()).thenReturn(brevlagerServiceBeanMock);
		return brevlagerServiceBeanMock;
	}

	static JoarkServiceBi mockJoarkService() throws BrevTechnicalException {
		mockStatic(JoarkServiceBeanFactory.class);
		JoarkServiceBeanFactory joarkServiceBeanFactoryMock = mock(JoarkServiceBeanFactory.class);
		when(JoarkServiceBeanFactory.getInstance()).thenReturn(joarkServiceBeanFactoryMock);
		JoarkServiceBi joarkServiceBiMock = mock(JoarkServiceBi.class);
		when(joarkServiceBeanFactoryMock.getJoarkService()).thenReturn(joarkServiceBiMock);
		return joarkServiceBiMock;
	}

	static FileConverter mockFileConverter() throws Exception {
		mockStatic(FileConverter.class);
		FileConverter fileConverterMock = mock(FileConverter.class);
		when(FileConverter.getInstance()).thenReturn(fileConverterMock);
		return fileConverterMock;
	}

	static XMLService mockXmlService() {
		mockStatic(XMLServiceFactory.class);
		XMLServiceFactory xmlServiceFactoryMock = mock(XMLServiceFactory.class);
		when(XMLServiceFactory.getInstance()).thenReturn(xmlServiceFactoryMock);
		XMLService xmlServiceMock = mock(XMLService.class);
		when(xmlServiceFactoryMock.createXMLService()).thenReturn(xmlServiceMock);
		return xmlServiceMock;
	}

	static MessageProducer mockMessageProducer() {
		mockStatic(MessageProducerFactory.class);
		MessageProducerFactory messageProducerFactoryMock = mock(MessageProducerFactory.class);
		when(MessageProducerFactory.getInstance()).thenReturn(messageProducerFactoryMock);
		MessageProducer messageProducerMock = mock(MessageProducer.class);
		when(messageProducerFactoryMock.createMessageProducer(isA(SystemType.class))).thenReturn(messageProducerMock);
		return messageProducerMock;
	}
}
