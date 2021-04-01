package no.nav.brevserver.consumer.joark.support;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostResponse;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.Journal;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.v1.binding.Journalbehandling;

/**
 * Base class for Joark delegates, contains common functionality.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public abstract class AbstractJoarkDelegate {

	static final String VARIANT_FORMAT_PRODUKSJON = "PRODUKSJON";
	static final String VARIANT_FORMAT_ARKIV = "ARKIV";
	static final String[] JOURNALSTATUS_LAGRE_INVALID_LIST = {"A", "FS", "FL"};

	protected Journal journalService;
	protected Journalbehandling journalbehandlingService;

	/**
	 * Gets a Journalpost from Joark.
	 *
	 * @param brevreferanse The journalpostId
	 * @return The journalpost
	 * @throws BrevTechnicalException if Joark call fails.
	 */
	protected Journalpost hentJournalpost(String brevreferanse) throws BrevTechnicalException {
		HentJournalpostRequest hentJournalpostRequest = createHentJournalpostRequest(brevreferanse);
		HentJournalpostResponse hentJournalpostResponse = null;
		try {
			hentJournalpostResponse = journalService.hentJournalpost(hentJournalpostRequest);
		} catch (HentJournalpostJournalpostIkkeFunnet e) {
			throw new BrevTechnicalException("Fant ikke journalpost med brevreferanse '" + brevreferanse + "' i JOARK", e);
		} catch (Exception e) {
			throw new BrevTechnicalException("HentJournalpost feilet", e);
		}
		return hentJournalpostResponse.getJournalpost();
	}

	protected HentJournalpostRequest createHentJournalpostRequest(String brevreferanse) throws BrevTechnicalException {
		HentJournalpostRequest hentJournalpostRequest = new HentJournalpostRequest();
		hentJournalpostRequest.setJournalpostId(getBrevreferanseAsLong(brevreferanse));
		return hentJournalpostRequest;
	}

	/**
	 * Tries to parse brevreferanse string to long.
	 *
	 * @param brevreferanse The brevreferanse.
	 * @return The parsed brevreferanse.
	 * @throws BrevTechnicalException if parsing of brevreferanse fails.
	 */
	protected long getBrevreferanseAsLong(String brevreferanse) throws BrevTechnicalException {
		try {
			return Long.valueOf(brevreferanse);
		} catch (NumberFormatException e) {
			throw new BrevTechnicalException("Ugyldig JournalpostID '" + brevreferanse + "' mottatt, kan ikke lagre i JOARK.");
		}
	}

	/**
	 * Setter for the journalService property.
	 *
	 * @param journalService the journalService to set
	 */
	public void setJournalService(Journal journalService) {
		this.journalService = journalService;
	}

	/**
	 * Setter for the journalbehandlingService property.
	 *
	 * @param journalbehandlingService the journalbehandlingService to set
	 */
	public void setJournalbehandlingService(Journalbehandling journalbehandlingService) {
		this.journalbehandlingService = journalbehandlingService;
	}

}
