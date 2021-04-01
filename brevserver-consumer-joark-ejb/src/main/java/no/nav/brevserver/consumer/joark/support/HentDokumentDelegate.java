package no.nav.brevserver.consumer.joark.support;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.DokumentInfo;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Fildetaljer;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentResponse;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentJournalpostRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.HentDokumentFilUuidFinnesIkke;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.HentDokumentJournalpostIkkeFunnet;
import no.nav.virksomhet.tjenester.arkiv.journal.v2.binding.HentJournalpostJournalpostIkkeFunnet;

/**
 * Class supporting retrieval of Document from Joark based on brevreferanse (journalpostId).
 *
 * @author Marius Thøring, Visma Sirius
 */
public class HentDokumentDelegate extends AbstractJoarkDelegate {

	/**
	 * Retrieves a Dokument from Joark. The Dokument is found by retrieving the Journalpost corresponding to 'brevreferanse' and
	 * the attached Fildetaljer with VariantFormat = ARKIV or PRODUKSJON.
	 *
	 * @param brevreferanse The journalpostId on which to retrieve the Dokument from.
	 * @return A BrevVO representing the Dokument.
	 * @throws BrevTechnicalException If an exception occurs.
	 */
	public BrevVO hentDokument(String brevreferanse) throws BrevTechnicalException {
		Journalpost journalpost = hentJournalpost(brevreferanse);
		String journalstatus = journalpost.getJournalstatus().getKode();

		String[] filUuidAndContentType = getFilUuid(journalpost);
		String filUuid = filUuidAndContentType[0];
		String contentType = filUuidAndContentType[1];

		HentDokumentRequest hentDokumentRequest = createHentDokumentRequest(brevreferanse, filUuid);
		HentDokumentResponse hentDokumentResponse = hentDokument(hentDokumentRequest);

		BrevVO brevVO = createBrevVO(brevreferanse, journalstatus, contentType, hentDokumentResponse.getDokument());
		return brevVO;
	}

	public boolean isJournalpost(String brevreferanse) throws BrevTechnicalException {
		try {
			HentJournalpostRequest hentJournalpostRequest = createHentJournalpostRequest(brevreferanse);
			journalService.hentJournalpost(hentJournalpostRequest);
		} catch (HentJournalpostJournalpostIkkeFunnet e) {
			return false;
		} catch (Exception e) {
			throw new BrevTechnicalException("HentJournalpost feilet", e);
		}
		return true;
	}

	private HentDokumentRequest createHentDokumentRequest(String brevreferanse, String filUuid) throws BrevTechnicalException {
		HentDokumentRequest hentDokumentRequest = new HentDokumentRequest();
		hentDokumentRequest.setJournalpostId(getBrevreferanseAsLong(brevreferanse));
		hentDokumentRequest.setFilUuId(filUuid);
		return hentDokumentRequest;
	}

	/**
	 * Extracts the filUUid from a Journalpost. If both fildetaljer with VariantFormat = ARKIV and VariantFormat = PRODUKSJON
	 * exists on the Journalpost, the filUUid of the first fildetaljer with VariantFormat = ARKIV is returned.
	 *
	 * @param journalpost The Journalpost on which to search for filUuid.
	 * @return The filUUid for the given Journalpost, or null if no filUUid is found.
	 */
	private String[] getFilUuid(Journalpost journalpost) throws BrevTechnicalException {
		DokumentInfo dokInfo = journalpost.getJournalpostDokumentInfoRelasjonListe().iterator().next().getDokumentInfo();

		Fildetaljer fildetaljerArkiv = null;
		Fildetaljer fildetaljerProd = null;

		for (Fildetaljer fildetaljer : dokInfo.getFildetaljerListe()) {
			if (!isZeroOrEmpty(fildetaljer.getFilstorrelse())) {
				if (fildetaljer.getVariantFormat().getKode().equals(VARIANT_FORMAT_ARKIV)) {
					fildetaljerArkiv = fildetaljer;
				} else if (fildetaljer.getVariantFormat().getKode().equals(VARIANT_FORMAT_PRODUKSJON)) {
					fildetaljerProd = fildetaljer;
				}
			}
		}

		String[] filUuidAndContentType = new String[2];
		if (fildetaljerArkiv != null) {
			filUuidAndContentType[0] = fildetaljerArkiv.getFilUuid();
			filUuidAndContentType[1] = FilType.PDF.getContentType();
		} else if (fildetaljerProd != null) {
			filUuidAndContentType[0] = fildetaljerProd.getFilUuid();
			String filtype = fildetaljerProd.getFiltype().getKode();
			if (filtype.equals(FilType.RTF.getJoarkCode())) {
				filUuidAndContentType[1] = FilType.RTF.getContentType();
			} else if (filtype.equals(FilType.DOCX.getJoarkCode())) {
				filUuidAndContentType[1] = FilType.DOCX.getContentType();
			} else {
				throw new BrevTechnicalException("Ukjent filtype '" + filtype + "' for " + VARIANT_FORMAT_PRODUKSJON
						+ "på journalpost med brevreferanse " + journalpost.getJournalpostId());
			}
		} else {
			throw new BrevTechnicalException("Fant ikke filUuid for variantformat " + VARIANT_FORMAT_PRODUKSJON + " eller "
					+ VARIANT_FORMAT_ARKIV + "på journalpost med brevreferanse " + journalpost.getJournalpostId());
		}
		return filUuidAndContentType;
	}

	private boolean isZeroOrEmpty(String string) {
		return string == null || string.equals("");
	}

	private BrevVO createBrevVO(String brevreferanse, String journalStatus, String contentType, byte[] brevdata) {
		BrevVO brevVO = new BrevVO();
		brevVO.setBrevreferanse(brevreferanse);
		brevVO.setLagerStatus(journalStatus);
		brevVO.setContentType(contentType);
		brevVO.setBrevdata(brevdata);
		return brevVO;
	}

	private HentDokumentResponse hentDokument(HentDokumentRequest hentDokumentRequest) throws BrevTechnicalException {
		HentDokumentResponse hentDokumentResponse = null;
		try {
			hentDokumentResponse = journalService.hentDokument(hentDokumentRequest);
		} catch (HentDokumentFilUuidFinnesIkke e) {
			throw new BrevTechnicalException("Fant ikke filUuid " + hentDokumentRequest.getFilUuId()
					+ " på journalpost med brevreferanse " + hentDokumentRequest.getJournalpostId(), e);
		} catch (HentDokumentJournalpostIkkeFunnet e) {
			throw new BrevTechnicalException("Fant ikke journalpost med brevreferanse "
					+ hentDokumentRequest.getJournalpostId(), e);
		} catch (Exception e) {
			throw new BrevTechnicalException("HentDokument feilet", e);
		}
		return hentDokumentResponse;
	}
}
