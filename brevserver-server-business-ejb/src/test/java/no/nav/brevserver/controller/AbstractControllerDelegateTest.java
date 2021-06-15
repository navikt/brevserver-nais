package no.nav.brevserver.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevSecurityException;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Log.class, ConfigManager.class, PerformanceLogger.class, BrevserverServiceFactory.class, BrevStatusVO.class })
public class AbstractControllerDelegateTest {

	private final static String SYSTEM_ID = "SOME SYSTEM_TYPE";
	private static final String BREVREFERANSE = "SOME BREVREFERANSE";
	private static final String TOKEN = "SOME BREVREFERANSE";

	private BrevserverService brevserverServiceBeanMock;
	private BrevStatusVO brevStatusVOMock;
	private Brevstatus brevstatusMock;

	private AbstractControllerDelegate abstractControllerDelegate;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		ControllerBeanDelegateTestUtility.mockLog();
		ControllerBeanDelegateTestUtility.mockConfigManager();
		ControllerBeanDelegateTestUtility.mockPerformanceLogger();
		mockStatic(BrevStatusVO.class);
		brevstatusMock = mock(Brevstatus.class);
		brevStatusVOMock = ControllerBeanDelegateTestUtility.mockBrevStatusVO(SYSTEM_ID, BREVREFERANSE, TOKEN, null);
		brevserverServiceBeanMock = ControllerBeanDelegateTestUtility.mockBrevserverService();
		abstractControllerDelegate = new AbstractControllerDelegate() {
		};
	}

	@Test
	public void shouldSaveDocument() throws BrevException {
		abstractControllerDelegate.lagreDokumentStatus(brevStatusVOMock);
		verify(brevserverServiceBeanMock).lagreBrevStatus(brevstatusMock, any());
	}

	@Test
	public void shouldVerifyAndAllowNewDocuments() throws BrevException {
		when(brevserverServiceBeanMock.hentBrevStatus(SYSTEM_ID, BREVREFERANSE)).thenReturn(null);
	
		abstractControllerDelegate.verifyChangeRequest(brevStatusVOMock);
	}

	@Test
	public void shouldVerifyAccess() throws BrevException {
		when(brevserverServiceBeanMock.sjekkTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN)).thenReturn(false);
		when(brevserverServiceBeanMock.hentBrevStatus(SYSTEM_ID, BREVREFERANSE)).thenReturn(brevstatusMock);

		try {
			abstractControllerDelegate.verifyChangeRequest(brevStatusVOMock);
		} catch (BrevSecurityException e) {
			assertTrue(e.isFeilkode(BrevSecurityException.IKKE_TILGANG_I_BREVSERVER));
			return;
		}
		fail("Should not get here!");
	}
	
	@Test
	public void shouldVerifyEditable() throws BrevException {
		brevStatusVOMock = ControllerBeanDelegateTestUtility.mockBrevStatusVO(SYSTEM_ID, BREVREFERANSE, TOKEN, Konstanter.BREVSTATUS_FERDIG);
		when(brevserverServiceBeanMock.sjekkTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN)).thenReturn(true);
		when(brevserverServiceBeanMock.hentBrevStatus(SYSTEM_ID, BREVREFERANSE)).thenReturn(brevstatusMock);

		try {
			abstractControllerDelegate.verifyChangeRequest(brevStatusVOMock);
		} catch (BrevFunctionalException e) {
			assertThat(e.getMessage(), containsString(Konstanter.BREVSTATUS_FERDIG));
			return;
		}
		fail("Should not get here!");
	}

	@Test
	public void shouldFailIfDocumentStatusIsNull() throws BrevException {
		try {
			abstractControllerDelegate.lagreDokumentStatus(null);
		} catch (IllegalArgumentException e) {
			return;
		}
		fail();
	}
}
