package no.nav.brevserver.consumer.joark;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevVO;

/**
 * Interface defining operations against Joark.
 * 
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public interface JoarkServiceBi {

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
	 * Retrieves a Dokument from Joark based on the given 'brevreferanse'
	 * 
	 * @param brevReferanse
	 *            The identifier of the Dokument to retrieve.
	 * @return A BrevVO representing the Dokument.
	 * @throws BrevTechnicalException
	 *             If an exception occurs.
	 */
	BrevVO hentDokument(String brevReferanse) throws BrevTechnicalException;
	
	
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
