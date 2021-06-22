package no.nav.brevserver.consumer.joark.support;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RunAs;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;

import no.nav.brevserver.consumer.joark.JoarkServiceBi;
import no.nav.brevserver.consumer.joark.map.OppdaterJournalRequestMapper;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.Journal;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.Journal_Service;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.Journalbehandling;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.Journalbehandling_Service;
import no.stelvio.common.context.RequestContext;
import no.stelvio.common.context.RequestContextHolder;
import no.stelvio.common.context.support.RequestContextSetter;
import no.stelvio.common.context.support.SimpleRequestContext;

/**
 * EJB 3 implementation of JoarkServiceBi.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@DeclareRoles(Konstanter.JOARK_SECURITY_ROLE)
@RunAs(Konstanter.JOARK_SECURITY_ROLE)
public class JoarkServiceBean implements JoarkServiceBi {

	private Journal journalService;
	private Journalbehandling journalbehandlingService;
	private LagreDokumentDelegate lagreDokumentDelegate;
	private HentDokumentDelegate hentDokumentDelegate;

	/**
	 * {@inheritDoc}
	 */
	public void lagreDokument(String brevreferanse, String contentType, byte[] brevdata) throws BrevTechnicalException {
		setRequestContextIfMissing();
		lagreDokumentDelegate.lagreDokument(brevreferanse, contentType, brevdata);
	}

	/**
	 * {@inheritDoc}
	 */
	public void lagreFerdigstiltDokument(String brevreferanse, BrevVO redBrevVO, BrevVO pdfBrevVO)
			throws BrevTechnicalException {
		setRequestContextIfMissing();
		lagreDokumentDelegate.lagreFerdigstiltDokument(brevreferanse, redBrevVO, pdfBrevVO);
	}

	/**
	 * {@inheritDoc}
	 */
	public BrevVO hentDokument(String brevreferanse) throws BrevTechnicalException {
		setRequestContextIfMissing();
		return hentDokumentDelegate.hentDokument(brevreferanse);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isJournalpost(String brevreferanse) throws BrevTechnicalException {
		setRequestContextIfMissing();
		return hentDokumentDelegate.isJournalpost(brevreferanse);
	}

	@PostConstruct
	public void initDelegates() {
		lagreDokumentDelegate = new LagreDokumentDelegate();
		lagreDokumentDelegate.setOppdaterJournalRequestMapper(new OppdaterJournalRequestMapper());
		lagreDokumentDelegate.setJournalService(journalService);
		lagreDokumentDelegate.setJournalbehandlingService(journalbehandlingService);

		hentDokumentDelegate = new HentDokumentDelegate();
		hentDokumentDelegate.setJournalService(journalService);
		hentDokumentDelegate.setJournalbehandlingService(journalbehandlingService);
	}

	/**
	 * The RequestContext must be set on the current thread as it is used to set the Stelvio Context header in the Joark JAX-WS
	 * calls.
	 */
	private void setRequestContextIfMissing() {
		if (!RequestContextHolder.isRequestContextSet()) {
			RequestContext requestContext = new SimpleRequestContext.Builder().userId("srvbrevserver")
					.componentId("Brevserver").build();
			RequestContextSetter.setRequestContext(requestContext);
		}
	}

	/**
	 * Setter for the journalService property.
	 *
	 * @param journalService the journalService to set
	 */
	@WebServiceRef(value = Journal_Service.class,
			wsdlLocation = "META-INF/wsdl/no/nav/virksomhet/tjenester/arkiv/journal/v2/Binding.wsdl")
	@HandlerChain(file = "JoarkClientHandler.xml")
	public void setJournalService(Journal journalService) {
		this.journalService = journalService;

		String endpoint = ConfigManager.getInstance().getString(ConfigManager.JOARK_JOURNAL_WS_URL, "");
		BindingProvider bindingProvider = (BindingProvider) journalService;
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
	}

	/**
	 * Setter for the journalbehandlingService property.
	 *
	 * @param journalbehandlingService the journalbehandlingService to set
	 */
	@WebServiceRef(value = Journalbehandling_Service.class,
			wsdlLocation = "META-INF/wsdl/no/nav/virksomhet/tjenester/arkiv/journalbehandling/v1/Binding.wsdl")
	@HandlerChain(file = "JoarkClientHandler.xml")
	public void setJournalbehandlingService(Journalbehandling journalbehandlingService) {
		this.journalbehandlingService = journalbehandlingService;

		String endpoint = ConfigManager.getInstance().getString(ConfigManager.JOARK_JOURNALBEHANDLING_WS_URL, "");
		BindingProvider bindingProvider = (BindingProvider) journalbehandlingService;
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
	}

}
