package no.nav.brevserver.joark;

import no.nav.brevserver.core.vo.BrevVO;

public interface JoarkService {

	/**
	 * Updates the content (the actual RTF or PDF file) of a Dokument in Joark. The Dokument is updated by first selecting the
	 * Journalpost to update based on data contained in 'kvittering', next the updated content is added to the Fildetaljer with
	 * VariantFormat = PRODUKSJON.
	 *
	 * @param brevreferanse
	 * @param contentType
	 * @param brevdata
	 */
	void lagreDokument(String brevreferanse, String contentType, byte[] brevdata);

	/**
	 * Updates the content of both the RTF and PDF file of Journalpost in Joark.
	 *
	 * @param brevreferanse
	 * @param redBrevVO
	 * @param pdfBrevVO
	 */
	void lagreFerdigstiltDokument(String brevreferanse, BrevVO redBrevVO, BrevVO pdfBrevVO);

	/**
	 * Retrieves a Dokument from Joark. The Dokument is found by retrieving the Journalpost corresponding to 'brevreferanse' and
	 * the attached Fildetaljer with VariantFormat = ARKIV or PRODUKSJON.
	 *
	 * @param brevreferanse The journalpostId on which to retrieve the Dokument from.
	 * @return A BrevVO representing the Dokument.
	 */
	BrevVO hentDokument(String brevreferanse);
}
