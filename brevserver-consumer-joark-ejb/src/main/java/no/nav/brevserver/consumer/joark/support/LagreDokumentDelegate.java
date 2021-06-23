package no.nav.brevserver.consumer.joark.support;

import no.nav.brevserver.consumer.joark.map.OppdaterJournalRequestMapper;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.DokumentInfo;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Fildetaljer;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.JournalpostDokumentInfoRelasjon;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.OppdaterJournalRequest;
import no.stelvio.common.context.RequestContextHolder;

/**
 * Implementation of the lagreDokument brevserver service.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class LagreDokumentDelegate extends AbstractJoarkDelegate {

	private OppdaterJournalRequestMapper oppdaterJournalRequestMapper;

	/**
	 * Updates the content (the actual RTF or PDF file) of a Dokument in Joark. The Dokument is updated by first selecting the
	 * Journalpost to update based on data contained in 'kvittering', next the updated content is added to the Fildetaljer with
	 * VariantFormat = PRODUKSJON.
	 *
	 * @param brevreferanse
	 * @param contentType
	 * @param brevdata
	 * @throws BrevTechnicalException
	 */
	public void lagreDokument(String brevreferanse, String contentType, byte[] brevdata) throws BrevTechnicalException {
		OppdaterJournalRequest oppdaterJournalRequest = createOppdaterJournalRequest(brevreferanse);
		setBrevDataOnRequest(contentType, brevdata, oppdaterJournalRequest);
		oppdaterJournal(oppdaterJournalRequest);
	}

	/**
	 * Updates the content of both the RTF and PDF file of Journalpost in Joark.
	 *
	 * @param brevreferanse
	 * @param redBrevVO
	 * @param pdfBrevVO
	 * @throws BrevTechnicalException
	 */
	public void lagreFerdigstiltDokument(String brevreferanse, BrevVO redBrevVO, BrevVO pdfBrevVO)
			throws BrevTechnicalException {
		OppdaterJournalRequest oppdaterJournalRequest = createOppdaterJournalRequest(brevreferanse);
		setBrevDataOnRequest(redBrevVO.getContentType(), redBrevVO.getBrevdata(), oppdaterJournalRequest);
		setBrevDataOnRequest(pdfBrevVO.getContentType(), pdfBrevVO.getBrevdata(), oppdaterJournalRequest);
		oppdaterJournal(oppdaterJournalRequest);
	}

	private OppdaterJournalRequest createOppdaterJournalRequest(String brevreferanse) throws BrevTechnicalException {
		Journalpost journalpost = hentJournalpost(brevreferanse);
		verifyJournalStatus(journalpost);
		OppdaterJournalRequest oppdaterJournalRequest = oppdaterJournalRequestMapper.map(journalpost);
		oppdaterJournalRequest.setEndretAvNavn(RequestContextHolder.currentRequestContext().getUserId());
		return oppdaterJournalRequest;
	}

	private void verifyJournalStatus(Journalpost journalpost) throws BrevTechnicalException {
		for (String invalidJournalstatus : JOURNALSTATUS_LAGRE_INVALID_LIST) {
			if (journalpost.getJournalstatus().getKode().equals(invalidJournalstatus)) {
				String msg = "Feil ved lagring/arkivering av dokument på journalpost med id '" + journalpost.getJournalpostId()
						+ "'. Journalstatus '" + journalpost.getJournalstatus().getKode()
						+ "' tillater ikke lagring/arkivering";
				throw new BrevTechnicalException(BrevTechnicalException.UGYLDIG_JOURNALSTATUS, msg, new Exception(msg));
			}
		}
	}

	private void setBrevDataOnRequest(String contentType, byte[] brevData, OppdaterJournalRequest oppdaterJournalRequest)
			throws BrevTechnicalException {
		Fildetaljer fildetaljer;
		if (FilType.PDF.getContentType().equals(contentType)) {
			fildetaljer = findFildetaljer(oppdaterJournalRequest, VARIANT_FORMAT_ARKIV, FilType.PDF.getJoarkCode());
			if (fildetaljer != null) {
				fildetaljer.setFiltypeKode(FilType.PDFA.getJoarkCode());
			}
		} else if (FilType.RTF.getContentType().equals(contentType)) {
			fildetaljer = findFildetaljer(oppdaterJournalRequest, VARIANT_FORMAT_PRODUKSJON, FilType.RTF.getJoarkCode());
		} else if (FilType.DOCX.getContentType().equals(contentType)) {
			fildetaljer = findFildetaljer(oppdaterJournalRequest, VARIANT_FORMAT_PRODUKSJON, FilType.RTF.getJoarkCode());
			if (fildetaljer != null) {
				fildetaljer.setFiltypeKode(FilType.DOCX.getJoarkCode());
			} else {
				fildetaljer = findFildetaljer(oppdaterJournalRequest, VARIANT_FORMAT_PRODUKSJON, FilType.DOCX.getJoarkCode());
			}
		} else {
			throw new BrevTechnicalException("Ugyldig filType '" + contentType + "' mottatt, kan ikke lagre i JOARK.");
		}
		if (fildetaljer == null) {
			throw new BrevTechnicalException("Fant ikke filDetaljer for filtype " + contentType
					+ " på journalpost med brevreferanse " + oppdaterJournalRequest.getJournalpostId());
		}
		fildetaljer.setFil(brevData);
	}

	private Fildetaljer findFildetaljer(OppdaterJournalRequest oppdaterJournalRequest, String variantFormat, String filtype)
			throws BrevTechnicalException {
		DokumentInfo dokumentInfo = getDokumentInfo(oppdaterJournalRequest);

		for (Fildetaljer fildetaljer : dokumentInfo.getFildetaljerListe()) {
			if (fildetaljer.getVariantFormatKode().equals(variantFormat) && fildetaljer.getFiltypeKode().equals(filtype)) {
				return fildetaljer;
			}
		}
		return null;
	}

	private DokumentInfo getDokumentInfo(OppdaterJournalRequest oppdaterJournalRequest) throws BrevTechnicalException {
		JournalpostDokumentInfoRelasjon dokumentInfoRelasjon = null;
		if (oppdaterJournalRequest.getJournalpostDokumentInfoRelasjonListe().iterator().hasNext()) {
			dokumentInfoRelasjon = oppdaterJournalRequest.getJournalpostDokumentInfoRelasjonListe().iterator().next();
		}
		if (dokumentInfoRelasjon == null) {
			throw new BrevTechnicalException("Fant ikke JournalpostDokumentInfoRelasjon på journalpost med brevreferanse "
					+ oppdaterJournalRequest.getJournalpostId());
		}
		DokumentInfo dokumentInfo = dokumentInfoRelasjon.getDokumentInfo();
		if (dokumentInfo == null) {
			throw new BrevTechnicalException("Fant ikke DokumentInfo på journalpost med brevreferanse "
					+ oppdaterJournalRequest.getJournalpostId());
		}
		return dokumentInfo;
	}

	private void oppdaterJournal(OppdaterJournalRequest oppdaterJournalRequest) throws BrevTechnicalException {
		try {
			journalbehandlingService.oppdaterJournal(oppdaterJournalRequest);
		} catch (Exception e) {
			throw new BrevTechnicalException("OppdaterJournal feilet", e);
		}
	}

	/**
	 * Setter for the oppdaterJournalRequestMapper property.
	 *
	 * @param oppdaterJournalRequestMapper the oppdaterJournalRequestMapper to set
	 */
	public void setOppdaterJournalRequestMapper(OppdaterJournalRequestMapper oppdaterJournalRequestMapper) {
		this.oppdaterJournalRequestMapper = oppdaterJournalRequestMapper;
	}

}
