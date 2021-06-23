package no.nav.brevserver.provider.itest;

import no.nav.brevserver.consumer.joark.factory.JoarkServiceBeanFactory;
import no.nav.brevserver.consumer.joark.support.JoarkServiceBean;
import no.nav.brevserver.querydsl.QTBrevlager5;
import no.nav.brevserver.querydsl.QTBrevstatus;
import no.nav.brevserver.querydsl.QTBrevtilgang;
import no.nav.brevserver.querydsl.TBrevlager5;
import no.nav.brevserver.querydsl.TBrevstatus;
import no.nav.brevserver.querydsl.TBrevtilgang;
import no.nav.brevserver.service.AbstractDatabaseTest;
import no.nav.brevserver.service.TempJndiHelper;
import no.nav.brevserver.service.brevlager.BrevlagerServiceFactory;
import no.nav.brevserver.service.brevlager.beans.BrevlagerServiceBean;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import no.nav.brevserver.service.brevserver.beans.BrevserverServiceBean;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.Journal;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.Journalbehandling;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.sql.DataSource;
import javax.xml.ws.BindingProvider;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Abstract provider testclass. Provides setup and utilities for provider integration tests.
 *
 * @author Joakim Bj�rnstad, Visma Consulting
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BrevserverServiceFactory.class, BrevlagerServiceFactory.class, JoarkServiceBeanFactory.class, TempJndiHelper.class})
public abstract class AbstractProviderTest extends AbstractDatabaseTest {

	private static final String NO_DB2_OPTIMIZATION = "";

	@Mock(extraInterfaces = BindingProvider.class)
	protected Journal journalServiceMock;
	@Mock(extraInterfaces = BindingProvider.class)
	protected Journalbehandling journalbehandlingServiceMock;

	private BrevserverServiceBean brevserverService;
	private BrevlagerServiceBean brevlagerServiceBean;
	private BrevlagerPopulator brevlagerPopulator;

	@Before
	public void setUpProviderTest() throws Exception {
		MockitoAnnotations.initMocks(this);

		brevlagerPopulator = new BrevlagerPopulator();
		brevlagerServiceBean = new BrevlagerServiceBean();
		brevserverService = new BrevserverServiceBean();

		brevlagerServiceBean.setDb2SingleRowOptimization(NO_DB2_OPTIMIZATION);
		brevserverService.setDb2SingleRowOptimization(NO_DB2_OPTIMIZATION);
		mockStatic(TempJndiHelper.class);
		DataSource dataSourceMock = jndiDataSource();
		when(TempJndiHelper.jndiDataSource()).thenReturn(dataSourceMock);
		setupBrevserverService();
		setupBrevlagerService();
		setupJoarkMock();
	}

	private void setupBrevserverService() {
		mockStatic(BrevserverServiceFactory.class);
		BrevserverServiceFactory brevserverServiceFactoryMock = mock(BrevserverServiceFactory.class);
		when(BrevserverServiceFactory.getInstance()).thenReturn(brevserverServiceFactoryMock);
		when(brevserverServiceFactoryMock.createBrevserverService()).thenReturn(brevserverService);
	}

	private void setupBrevlagerService() {
		mockStatic(BrevlagerServiceFactory.class);
		BrevlagerServiceFactory brevlagerServiceFactoryMock = mock(BrevlagerServiceFactory.class);
		when(BrevlagerServiceFactory.getInstance()).thenReturn(brevlagerServiceFactoryMock);
		when(brevlagerServiceFactoryMock.createBrevlagerService()).thenReturn(brevlagerServiceBean);
	}

	private void setupJoarkMock() throws Exception {
		mockStatic(JoarkServiceBeanFactory.class);
		JoarkServiceBeanFactory joarkServiceBeanFactoryMock = Mockito.mock(JoarkServiceBeanFactory.class);
		when(JoarkServiceBeanFactory.getInstance()).thenReturn(joarkServiceBeanFactoryMock);

		JoarkServiceBean joarkServiceBean = new JoarkServiceBean();
		joarkServiceBean.setJournalService(journalServiceMock);
		joarkServiceBean.setJournalbehandlingService(journalbehandlingServiceMock);
		joarkServiceBean.initDelegates();
		when(joarkServiceBeanFactoryMock.getJoarkService()).thenReturn(joarkServiceBean);
	}

	protected void insertBrevtilgang(String brevreferanse, String token, String systemId) {
		QTBrevtilgang e = new QTBrevtilgang("t");
		TBrevtilgang brevtilgang = brevlagerPopulator.createBrevtilgang(brevreferanse, token, systemId);
		insert(e).populate(brevtilgang).execute();
	}

	protected void insertBrevstatus(String brevreferanse, String systemId, String status) {
		QTBrevstatus e = new QTBrevstatus("s");
		TBrevstatus brevstatus = brevlagerPopulator.createBrevstatus(brevreferanse, systemId, status);
		insert(e).populate(brevstatus).execute();
	}

	protected void insertBrevlager(String brevreferanse, String systemId, String status, String contentType, byte[] brevdata) {
		QTBrevlager5 e = new QTBrevlager5("b");
		TBrevlager5 brevlager = brevlagerPopulator.createBrevlager(brevreferanse, systemId, status, contentType, brevdata);
		insert(e).populate(brevlager).execute();
	}

	protected TBrevstatus getBrevstatus(String brevreferanse, String systemId) {
		QTBrevstatus e = new QTBrevstatus("s");
		return query().from(e).where(e.brevreferanse.eq(brevreferanse).and(e.systemid.eq(systemId))).uniqueResult(e);
	}

	protected TBrevlager5 getBrevlager(String brevreferanse, String systemId) {
		QTBrevlager5 e = new QTBrevlager5("b");
		return query().from(e).where(e.brevreferanse.eq(brevreferanse).and(e.systemid.eq(systemId))).uniqueResult(e);
	}
}
