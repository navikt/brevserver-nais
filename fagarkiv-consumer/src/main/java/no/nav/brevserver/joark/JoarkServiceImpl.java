package no.nav.brevserver.joark;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Qualifier("joarkService")
@Service
@Slf4j
public class JoarkServiceImpl implements JoarkService {

	static final String VARIANT_FORMAT_PRODUKSJON = "PRODUKSJON";
	static final String VARIANT_FORMAT_ARKIV = "ARKIV";

	private final JournalClient journalClient;

	public JoarkServiceImpl(JournalClient journalClient) {
		this.journalClient = journalClient;
	}

	@Override
	public void lagreDokument(String brevreferanse, String contentType, byte[] brevdata) throws BrevTechnicalException {
		throw new UnsupportedOperationException("lagreDokument mot joark er ikke støttet");
	}

	@Override
	public void lagreFerdigstiltDokument(String brevreferanse, BrevVO redBrevVO, BrevVO pdfBrevVO) {
		throw new UnsupportedOperationException("lagreFerdigstiltDokument mot joark er ikke støttet");
	}

	@Override
	public BrevVO hentDokument(String brevreferanse) throws BrevTechnicalException {
		Journalpost journalpost = journalClient.hentJournalpost(getBrevreferanseAsLong(brevreferanse));
		String journalstatus = journalpost.getJournalstatus().getKode();

		String[] filUuidAndContentType = getFilUuid(journalpost);
		String filUuid = filUuidAndContentType[0];
		String contentType = filUuidAndContentType[1];

		HentDokumentRequest hentDokumentRequest = createHentDokumentRequest(brevreferanse, filUuid);
		HentDokumentResponse hentDokumentResponse = hentDokument(hentDokumentRequest);

		return createBrevVO(brevreferanse, journalstatus, contentType, hentDokumentResponse.getDokument());
	}

	/**
	 * Extracts the filUUid from a Journalpost. If both fildetaljer with VariantFormat = ARKIV and VariantFormat = PRODUKSJON
	 * exists on the Journalpost, the filUUid of the first fildetaljer with VariantFormat = ARKIV is returned.
	 *
	 * @param journalpost The Journalpost on which to search for filUuid.
	 * @return The filUUid for the given Journalpost, or null if no filUUid is found.
	 */
	private String[] getFilUuid(Journalpost journalpost) throws BrevTechnicalException {
		no.nav.virksomhet.gjennomforing.arkiv.journal.v2.DokumentInfo dokInfo = journalpost.getJournalpostDokumentInfoRelasjonListe().getFirst().getDokumentInfo();

		no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Fildetaljer fildetaljerArkiv = null;
		no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Fildetaljer fildetaljerProd = null;

		for (no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Fildetaljer fildetaljer : dokInfo.getFildetaljerListe()) {
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
		return string == null || string.isEmpty();
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
		try {
			return journalClient.hentDokument(hentDokumentRequest);
		} catch (Exception e) {
			throw new BrevTechnicalException("HentDokument feilet", e);
		}
	}

	private HentDokumentRequest createHentDokumentRequest(String brevreferanse, String filUuid) throws BrevTechnicalException {
		HentDokumentRequest hentDokumentRequest = new HentDokumentRequest();
		hentDokumentRequest.setJournalpostId(getBrevreferanseAsLong(brevreferanse));
		hentDokumentRequest.setFilUuId(filUuid);
		return hentDokumentRequest;
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
			return Long.parseLong(brevreferanse);
		} catch (NumberFormatException e) {
			throw new BrevTechnicalException("Ugyldig JournalpostID '" + brevreferanse + "' mottatt, kan ikke lagre i JOARK.");
		}
	}
}
