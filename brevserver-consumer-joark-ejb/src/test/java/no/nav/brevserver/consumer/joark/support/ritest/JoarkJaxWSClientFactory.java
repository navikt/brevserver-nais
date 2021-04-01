package no.nav.brevserver.consumer.joark.support.ritest;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.Journal;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.Journalbehandling;

/**
 * 
 * Factory for obtaining instances of {@link Journal} and {@link Journalbehandling}.
 * 
 * @author Emil Urnes, Visma Consulting
 */
public enum JoarkJaxWSClientFactory {

	INSTANCE;

	private Journal journalService;
	private Journalbehandling journalbehandlingService;

	private JoarkJaxWSClientFactory() {
		internalCreateJournalService();
		internalCreateJournalbehandlingService();
	}

	/**
	 * Convience method.
	 * 
	 * @return The INSTANCE of {@link JoarkJaxWSClientFactory}.
	 */
	public static JoarkJaxWSClientFactory getInstance() throws BrevTechnicalException {
		return INSTANCE;
	}

	private void internalCreateJournalService() {
		URL endpoint;
		try {
			endpoint = new URL(ConfigManager.getInstance().getString(ConfigManager.JOARK_JOURNAL_WS_URL, ""));
		} catch (MalformedURLException e) {
			throw new RuntimeException("URL invalid: " + ConfigManager.JOARK_JOURNAL_WS_URL);
		}
		final String namespaceUri = "http://nav.no/virksomhet/tjenester/arkiv/journal/v2";
		final QName serviceQName = new QName(namespaceUri, "JournalWSEXP_JournalHttpService");
		final QName portQName = new QName(namespaceUri, "JournalWSEXP_JournalHttpPort");
		Service service = Service.create(endpoint, serviceQName);
		service.setHandlerResolver(new JoarkConsumerHandlerChain());
		this.journalService = service.getPort(portQName, Journal.class);
	}

	private void internalCreateJournalbehandlingService() {
		URL endpoint;
		try {
			endpoint = new URL(ConfigManager.getInstance().getString(ConfigManager.JOARK_JOURNALBEHANDLING_WS_URL, ""));
		} catch (MalformedURLException e) {
			throw new RuntimeException("URL invalid: " + ConfigManager.JOARK_JOURNALBEHANDLING_WS_URL);
		}
		final String namespaceUri = "http://nav.no/virksomhet/tjenester/arkiv/journalbehandling/v1";
		final QName serviceQName = new QName(namespaceUri, "JournalbehandlingWSEXP_JournalbehandlingHttpService");
		final QName portQName = new QName(namespaceUri, "JournalbehandlingWSEXP_JournalbehandlingHttpPort");
		Service service = Service.create(endpoint, serviceQName);
		service.setHandlerResolver(new JoarkConsumerHandlerChain());
		this.journalbehandlingService = service.getPort(portQName, Journalbehandling.class);
	}

	/**
	 * Getter for the journalService property.
	 * 
	 * @return An instance of Journal (or null).
	 */
	public Journal getJournalService() {
		return this.journalService;
	}

	/**
	 * Getter for the journalbehandlingService property.
	 * 
	 * @return An instance of Journalbehandling (or null).
	 */
	public Journalbehandling getJournalbehandlingService() {
		return this.journalbehandlingService;
	}

}
