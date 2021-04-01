package no.nav.brevserver.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.brevserver.consumer.joark.JoarkServiceBi;
import no.nav.brevserver.consumer.joark.factory.JoarkServiceBeanFactory;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.KnappStatus;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevSecurityException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.log.Log;
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
import no.nav.brevserver.service.jms.MessageProducerFactory;
import no.nav.brevserver.service.xml.XMLServiceFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class, ConfigManager.class, PerformanceLogger.class, BrevlagerServiceFactory.class, BrevserverServiceFactory.class,
        JoarkServiceBeanFactory.class, FileConverter.class, XMLServiceFactory.class, MessageProducerFactory.class})
public class HentControllerDelegateTest {

    private static final String BREVREFERANSE = "SOME BREVREFERANSE";
    private static final String SYSTEM_ID_PE = "PE";
    private static final String SYSTEM_ID_BI = "BI";
    private static final String STATUS_KASSERT = "KASSERT";
    private static final String TOKEN = "SOME TOKEN";

    private static final String JOURNALSTATUS_NORMAL = "D";
    private static final String JOURNALSTATUS_AVBRUTT = "A";
    private static final byte[] BREVDATA_RTF = new byte[]{'R', 'T', 'F'};
    private static final byte[] BREVDATA_PDF = new byte[]{'P', 'D', 'F'};
    private static final byte[] BREVDATA_KONVERTERT = new byte[]{'K', 'O', 'N'};

    private BrevserverService brevserverServiceBeanMock;
    private BrevlagerService brevlagerServiceBeanMock;
    private JoarkServiceBi joarkServiceBiMock;
    private FileConverter fileConverterMock;
    private BrevStatusVO brevStatusMock;
    private HentControllerDelegate controllerBean;

    @Captor
    private ArgumentCaptor<KvitteringVO> kvitteringVOCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ControllerBeanDelegateTestUtility.mockLog();
        ControllerBeanDelegateTestUtility.mockConfigManager();
        ControllerBeanDelegateTestUtility.mockPerformanceLogger();
        brevserverServiceBeanMock = ControllerBeanDelegateTestUtility.mockBrevserverService();
        giveAccess(true);
        brevlagerServiceBeanMock = ControllerBeanDelegateTestUtility.mockBrevlagerService();
        joarkServiceBiMock = ControllerBeanDelegateTestUtility.mockJoarkService();
        fileConverterMock = ControllerBeanDelegateTestUtility.mockFileConverter();
        when(fileConverterMock.convertToPdf(BREVDATA_RTF)).thenReturn(BREVDATA_KONVERTERT);
        brevStatusMock = ControllerBeanDelegateTestUtility.mockBrevStatusVO(SYSTEM_ID_PE, BREVREFERANSE, TOKEN, null);
        controllerBean = new HentControllerDelegate();
        controllerBean.setLog(mock(Log.class));
    }

    @Test
    public void hentDokumentShouldFailIfBrevstatusIsNull() throws BrevException {
        try {
            controllerBean.hentDokument(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("Should not get here!");
    }

    @Test
    public void shouldFailIfNoAccess() throws BrevException {
        giveAccess(false);
        try {
            controllerBean.hentDokument(brevStatusMock);
            fail("Should disallow access");
        } catch (BrevSecurityException e) {
            assertTrue(e.isFeilkode(BrevSecurityException.IKKE_TILGANG_I_BREVSERVER));
            verify(brevserverServiceBeanMock).sjekkTilgang(SYSTEM_ID_PE, BREVREFERANSE, TOKEN);
        }
    }

    @Test
    public void hentDokumentBiShouldCallJoarkCorrectly() throws BrevException {
        brevStatusMock = ControllerBeanDelegateTestUtility.mockBrevStatusVO(SYSTEM_ID_PE, BREVREFERANSE, TOKEN, STATUS_KASSERT);
        BrevVO brevVOMock = ControllerBeanDelegateTestUtility.mockBrevVO(SYSTEM_ID_PE, JOURNALSTATUS_NORMAL,
                FilType.RTF.getContentType(), BREVDATA_RTF);
        when(joarkServiceBiMock.hentDokument(isA(String.class))).thenReturn(brevVOMock);

        BrevVO resultBrevVO = controllerBean.hentDokument(brevStatusMock);

        verify(joarkServiceBiMock).hentDokument(BREVREFERANSE);
        assertThat(resultBrevVO, equalTo(brevVOMock));
    }

    @Test
    public void hentDokumentPeShouldConvertOnlyIfAvbruttRtf() throws Exception {
        hentDokumentPeShouldConvertFileIfAvbruttRtf();
        hentDokumentPeShouldNotConvertFileIfAvbruttPdf();
        hentDokumentPeShouldNotConvertFileIfNormalRtf();
    }

    private void hentDokumentPeShouldConvertFileIfAvbruttRtf() throws Exception {
        BrevVO brevVOMock = ControllerBeanDelegateTestUtility.mockBrevVO(SYSTEM_ID_PE, JOURNALSTATUS_AVBRUTT,
                FilType.RTF.getContentType(), BREVDATA_RTF);
        when(joarkServiceBiMock.hentDokument(isA(String.class))).thenReturn(brevVOMock);

        controllerBean.hentDokument(brevStatusMock);
        verify(fileConverterMock).convertToPdf(BREVDATA_RTF);
        verify(brevVOMock, times(1)).setBrevdata(BREVDATA_KONVERTERT);
        verify(brevVOMock, times(1)).setContentType(FilType.PDF.getContentType());
    }

    private void hentDokumentPeShouldNotConvertFileIfNormalRtf() throws Exception {
        BrevVO brevVOMock = ControllerBeanDelegateTestUtility.mockBrevVO(SYSTEM_ID_PE, JOURNALSTATUS_NORMAL,
                FilType.RTF.getContentType(), BREVDATA_RTF);
        when(joarkServiceBiMock.hentDokument(isA(String.class))).thenReturn(brevVOMock);
        controllerBean.hentDokument(brevStatusMock);
        verify(brevVOMock, times(0)).setBrevdata(isA(byte[].class));
        verify(brevVOMock, times(0)).setContentType(isA(String.class));
    }

    private void hentDokumentPeShouldNotConvertFileIfAvbruttPdf() throws Exception {
        BrevVO brevVOMock = ControllerBeanDelegateTestUtility.mockBrevVO(SYSTEM_ID_PE, JOURNALSTATUS_AVBRUTT,
                FilType.PDF.getContentType(), BREVDATA_PDF);
        when(joarkServiceBiMock.hentDokument(isA(String.class))).thenReturn(brevVOMock);

        controllerBean.hentDokument(brevStatusMock);
        verify(brevVOMock, times(0)).setBrevdata(isA(byte[].class));
        verify(brevVOMock, times(0)).setContentType(isA(String.class));
    }

    @Test
    public void hentDokumentBiShouldCallBrevlagerServiceCorrectly() throws Exception {
        brevStatusMock = ControllerBeanDelegateTestUtility.mockBrevStatusVO(SYSTEM_ID_BI, BREVREFERANSE, TOKEN, null);
        BrevVO brevVOMock = mock(BrevVO.class);
        when(brevlagerServiceBeanMock.getBrev(SYSTEM_ID_BI, BREVREFERANSE)).thenReturn(brevVOMock);

        BrevVO resultBrevVO = controllerBean.hentDokument(brevStatusMock);

        verify(brevlagerServiceBeanMock).getBrev(SYSTEM_ID_BI, BREVREFERANSE);
        assertThat(resultBrevVO, equalTo(brevVOMock));
    }

    @Test
    public void hentDokumentShouldFailIfBrevlagerServiceReturnsNull() throws BrevTechnicalException {
        brevStatusMock = ControllerBeanDelegateTestUtility.mockBrevStatusVO(SYSTEM_ID_BI, BREVREFERANSE, TOKEN, null);
        when(brevlagerServiceBeanMock.getBrev(SYSTEM_ID_BI, BREVREFERANSE)).thenReturn(null);

        try {
            controllerBean.hentDokument(brevStatusMock);
        } catch (BrevException e) {
            verify(brevlagerServiceBeanMock).getBrev(SYSTEM_ID_BI, BREVREFERANSE);
            return;
        }
        fail("Should not get here!");
    }

    @Test
    public void hentDokumentBiShouldFailIfStatusKassert() throws BrevException {
        brevStatusMock = ControllerBeanDelegateTestUtility.mockBrevStatusVO(SYSTEM_ID_BI, BREVREFERANSE, TOKEN, STATUS_KASSERT);
        BrevVO brevVOMock = ControllerBeanDelegateTestUtility.mockBrevVO(SYSTEM_ID_BI, STATUS_KASSERT, "dummy", "dummy".getBytes());
        when(brevlagerServiceBeanMock.getBrev(SYSTEM_ID_BI, BREVREFERANSE)).thenReturn(brevVOMock);

        try {
            controllerBean.hentDokument(brevStatusMock);
        } catch (BrevFunctionalException e) {
            assertTrue(e.isFeilkode(BrevFunctionalException.DOKUMENTET_ER_FLAGGET_FOR_KASSASJON));
            return;
        }
        fail("Should not get here!");
    }

    @Test
    public void hentKnappStatusShouldReturnKnappStatus() throws BrevException {
        when(brevserverServiceBeanMock.hentBrevStatus(SYSTEM_ID_BI, BREVREFERANSE)).thenReturn(brevStatusMock);

        KnappStatus knappStatus = controllerBean.hentKnappStatus(SYSTEM_ID_BI, BREVREFERANSE);
        assertThat(knappStatus, is(brevStatusMock.getKnappStatus()));
    }

    @Test
    public void hentKnappStatusShouldReturnDefaultIfBrevserverServiceReturnsNull() throws BrevException {
        when(brevserverServiceBeanMock.hentBrevStatus(SYSTEM_ID_BI, BREVREFERANSE)).thenReturn(null);

        KnappStatus knappStatus = controllerBean.hentKnappStatus(SYSTEM_ID_BI, BREVREFERANSE);
        assertThat(knappStatus.toString(), is(Integer.toString(KnappStatus.getDefaultValue())));
    }

    private void giveAccess(boolean access) throws BrevTechnicalException {
        when(brevserverServiceBeanMock.sjekkTilgang(SYSTEM_ID_PE, BREVREFERANSE, TOKEN)).thenReturn(access);
        when(brevserverServiceBeanMock.sjekkTilgang(SYSTEM_ID_BI, BREVREFERANSE, TOKEN)).thenReturn(access);
    }
}
