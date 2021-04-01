package no.nav.brevserver.controller;

import no.nav.brevserver.consumer.joark.factory.JoarkServiceBeanFactory;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevSecurityException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.service.brevlager.BrevlagerService;
import no.nav.brevserver.service.brevlager.BrevlagerServiceFactory;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import no.nav.brevserver.service.converter.FileConverter;
import no.nav.brevserver.service.jms.MessageProducer;
import no.nav.brevserver.service.jms.MessageProducerFactory;
import no.nav.brevserver.service.xml.XMLService;
import no.nav.brevserver.service.xml.XMLServiceFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class, ConfigManager.class, PerformanceLogger.class, BrevlagerServiceFactory.class, BrevserverServiceFactory.class,
		JoarkServiceBeanFactory.class, FileConverter.class, XMLServiceFactory.class, MessageProducerFactory.class})
public class LagreControllerDelegateTest {

	private static final String BREVREFERANSE = "SOME BREVREFERANSE";
	private static final String SYSTEM_ID_PE = "PE";
	private static final String SYSTEM_ID_BI = "BI";
	private static final String RECEIPT = "SOME XML IN STRING FORM";
	private static final String TOKEN = "SOME TOKEN";

	private static final byte[] BREVDATA_RTF = new byte[]{'R', 'T', 'F'};
	private static final byte[] BREVDATA_KONVERTERT = new byte[]{'K', 'O', 'N'};

	private BrevserverService brevserverServiceBeanMock;
	private BrevlagerService brevlagerServiceBeanMock;
	private FileConverter fileConverterMock;
	private BrevStatusVO brevStatusVOMock;
	private LagreControllerDelegate controllerBean;
	private XMLService xmlServiceMock;
	private MessageProducer messageProducerMock;

	@Mock
	private BrevVO brevVOMock;

	@Captor
	private ArgumentCaptor<KvitteringVO> kvitteringVOCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		ControllerBeanDelegateTestUtility.mockLog();
		ControllerBeanDelegateTestUtility.mockConfigManager();
		ControllerBeanDelegateTestUtility.mockPerformanceLogger();
		brevserverServiceBeanMock = ControllerBeanDelegateTestUtility.mockBrevserverService();
		when(brevserverServiceBeanMock.sjekkTilgang(isA(String.class), isA(String.class), isA(String.class))).thenReturn(true);
		brevlagerServiceBeanMock = ControllerBeanDelegateTestUtility.mockBrevlagerService();
		ControllerBeanDelegateTestUtility.mockJoarkService();
		fileConverterMock = ControllerBeanDelegateTestUtility.mockFileConverter();
		when(fileConverterMock.convertToPdf(BREVDATA_RTF)).thenReturn(BREVDATA_KONVERTERT);
		brevStatusVOMock = ControllerBeanDelegateTestUtility.mockBrevStatusVO(SYSTEM_ID_PE, BREVREFERANSE, TOKEN, null);
		xmlServiceMock = ControllerBeanDelegateTestUtility.mockXmlService();
		messageProducerMock = ControllerBeanDelegateTestUtility.mockMessageProducer();
		controllerBean = new LagreControllerDelegate();
		controllerBean.setLog(mock(Log.class));
	}

	@Test
	public void lagreDokumentShouldFailIfNullParameters() throws BrevException {
		try {
			controllerBean.lagreDokument(null, brevStatusVOMock, null);
			fail("Should not get here!");
		} catch (IllegalArgumentException e) {
		}
		try {
			controllerBean.lagreDokument(brevVOMock, null, null);
			fail("Should not get here!");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void shouldFailIfNoAccess() throws BrevException {
		giveAccess(false);
		try {
			controllerBean.lagreDokument(mock(BrevVO.class), brevStatusVOMock, SystemType.BI);
			fail("Should disallow access");
		} catch (BrevSecurityException e) {
			assertTrue(e.isFeilkode(BrevSecurityException.IKKE_TILGANG_I_BREVSERVER));
			verify(brevserverServiceBeanMock).hentBrevStatus(SYSTEM_ID_PE, BREVREFERANSE);
			verify(brevserverServiceBeanMock).sjekkTilgang(SYSTEM_ID_PE, BREVREFERANSE, TOKEN);
		}
	}

	@Test
	public void lagreBrevlagerDokumentShouldCallBrevlagerServiceCorrectly() throws BrevException {
		brevStatusVOMock = ControllerBeanDelegateTestUtility.mockBrevStatusVO(SYSTEM_ID_BI, BREVREFERANSE, TOKEN, null);
		when(brevlagerServiceBeanMock.lagreBrev(brevVOMock, brevStatusVOMock)).thenReturn(mock(BrevStatusVO.class));

		controllerBean.lagreDokument(brevVOMock, brevStatusVOMock, SystemType.BI);

		verify(brevlagerServiceBeanMock).lagreBrev(brevVOMock, brevStatusVOMock);
	}

	@Test
	public void lagreBrevlagerDokumentShouldSendReceipt() throws BrevException {
		BrevVO brevVOMock = ControllerBeanDelegateTestUtility.mockBrevVO(SYSTEM_ID_BI, null, FilType.RTF.getContentType(),
				BREVDATA_RTF);
		BrevStatusVO brevStatusVOMock = ControllerBeanDelegateTestUtility.mockBrevStatusVO(SYSTEM_ID_BI, BREVREFERANSE, TOKEN, null);
		BrevStatusVO oldBrevStatusVOMock = mock(BrevStatusVO.class);

		when(brevlagerServiceBeanMock.lagreBrev(brevVOMock, brevStatusVOMock)).thenReturn(oldBrevStatusVOMock);
		when(xmlServiceMock.unmarshal(isA(KvitteringVO.class), isA(BrevStatusVO.class))).thenReturn(RECEIPT);

		controllerBean.lagreDokument(brevVOMock, brevStatusVOMock, SystemType.BI);

		verify(xmlServiceMock).unmarshal(kvitteringVOCaptor.capture(), eq(brevStatusVOMock));
		KvitteringVO kvitteringVO = kvitteringVOCaptor.getValue();
		assertThat(kvitteringVO.getBrevreferanse(), equalTo(brevStatusVOMock.getBrevreferanse()));
		assertThat(kvitteringVO.getSystemID(), equalTo(brevStatusVOMock.getSystemID()));
		assertThat(kvitteringVO.getLagerStatus(), equalTo(brevStatusVOMock.getStatus()));
		assertThat(kvitteringVO.getContentType(), equalTo(brevVOMock.getContentType()));

		verify(messageProducerMock).sendReturMelding(brevStatusVOMock.getReturKoe(), false, null, RECEIPT);
	}

	@Test
	public void ferdigstillDokumentShouldCallBrevlagerServiceCorrectly() throws BrevException {
		brevStatusVOMock = ControllerBeanDelegateTestUtility.mockBrevStatusVO(SYSTEM_ID_BI, BREVREFERANSE, TOKEN, Konstanter.BREVSTATUS_LAGRET_KLADD);
		BrevVO brevVORtfMock = mock(BrevVO.class);
		BrevVO brevVOPdfMock = mock(BrevVO.class);
		when(brevserverServiceBeanMock.hentBrevStatus(SYSTEM_ID_BI, BREVREFERANSE)).thenReturn(brevStatusVOMock);
		controllerBean.ferdigstillDokument(brevStatusVOMock, brevVORtfMock, brevVOPdfMock, SystemType.BI);

		verify(brevlagerServiceBeanMock).ferdigstillBrev(brevStatusVOMock, brevVORtfMock, brevVOPdfMock);
	}

	private void giveAccess(boolean access) throws BrevTechnicalException {
		when(brevserverServiceBeanMock.hentBrevStatus(SYSTEM_ID_PE, BREVREFERANSE)).thenReturn(brevStatusVOMock);
		when(brevserverServiceBeanMock.sjekkTilgang(SYSTEM_ID_PE, BREVREFERANSE, TOKEN)).thenReturn(access);
	}
}
