package no.nav.brevserver.fagarkiv.dokarkiv;

import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;

/**
 * Interface defining operations against Dokarkiv, moved from Joark.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public interface DokarkivConsumer {

	/**
	 * Updates the content (the actual RTF of PDF file) of a Dokument in Joark. The Dokument is updated by first selecting the
	 * Journalpost to update based on data contained in 'kvittering', next the updated content is added to the Fildetaljer with
	 * VariantFormat = PRODUKSJON.
	 *
	 * @param brevreferanse
	 * @param contentType
	 * @param brevData
	 * @throws BrevTechnicalException
	 */
	void lagreDokument(String brevreferanse, String contentType, byte[] brevData) throws BrevTechnicalException;

	/**
	 * Updates the content of both the RTF and PDF file of Journalpost in Joark.
	 *
	 * @param brevreferanse
	 * @param redBrevVO
	 * @param pdfBrevVO
	 * @throws BrevTechnicalException
	 */
	void lagreFerdigstiltDokument(String brevreferanse, BrevVO redBrevVO, BrevVO pdfBrevVO) throws BrevTechnicalException;

	/**
	 * Checks if a journalpost exists based on 'brevreferanse'
	 *
	 * @param brevReferanse
	 *            The identifier of the Dokument to retrieve.
	 * @return
	 * @throws BrevTechnicalException
	 *             If an exception occurs.
	 */
	boolean isJournalpost(String brevReferanse) throws BrevTechnicalException;

}
