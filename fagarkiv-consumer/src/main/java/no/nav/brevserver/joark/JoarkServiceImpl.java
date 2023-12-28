package no.nav.brevserver.joark;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.utils.stelvio.RequestContextHolder;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.fagarkiv.mapper.OppdaterJournalRequestMapper;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentRequest;
import no.nav.virksomhet.tjenester.arkiv.journal.meldinger.v2.HentDokumentResponse;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.DokumentInfo;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.Fildetaljer;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.JournalpostDokumentInfoRelasjon;
import no.nav.virksomhet.tjenester.arkiv.journalbehandling.meldinger.v1.OppdaterJournalRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JoarkServiceImpl implements JoarkService {

	static final String VARIANT_FORMAT_PRODUKSJON = "PRODUKSJON";
	static final String VARIANT_FORMAT_ARKIV = "ARKIV";
	static final String[] JOURNALSTATUS_LAGRE_INVALID_LIST = {"A", "FS", "FL"};

	private final JournalClient journalClient;
	private final JournalbehandlingClient journalbehandlingClient;

	private final OppdaterJournalRequestMapper oppdaterJournalRequestMapper;

	public JoarkServiceImpl(JournalClient journalClient,
							JournalbehandlingClient journalbehandlingClient,
							OppdaterJournalRequestMapper oppdaterJournalRequestMapper) {
		this.journalClient = journalClient;
		this.journalbehandlingClient = journalbehandlingClient;
		this.oppdaterJournalRequestMapper = oppdaterJournalRequestMapper;
	}

	@Override
	public void lagreDokument(String brevreferanse, String contentType, byte[] brevdata) throws BrevTechnicalException {
		OppdaterJournalRequest oppdaterJournalRequest = createOppdaterJournalRequest(brevreferanse);
		setBrevDataOnRequest(contentType, brevdata, oppdaterJournalRequest);
		journalbehandlingClient.oppdaterJournalpost(oppdaterJournalRequest);
	}

	@Override
	public void lagreFerdigstiltDokument(String brevreferanse, BrevVO redBrevVO, BrevVO pdfBrevVO)
			throws BrevTechnicalException {
		OppdaterJournalRequest oppdaterJournalRequest = createOppdaterJournalRequest(brevreferanse);
		setBrevDataOnRequest(redBrevVO.getContentType(), redBrevVO.getBrevdata(), oppdaterJournalRequest);
		setBrevDataOnRequest(pdfBrevVO.getContentType(), pdfBrevVO.getBrevdata(), oppdaterJournalRequest);
		journalbehandlingClient.oppdaterJournalpost(oppdaterJournalRequest);
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

	private OppdaterJournalRequest createOppdaterJournalRequest(String brevreferanse) throws BrevTechnicalException {
		Journalpost journalpost = journalClient.hentJournalpost(getBrevreferanseAsLong(brevreferanse));
		verifyNotEmptyBruker(journalpost);
		verifyJournalStatus(journalpost);
		OppdaterJournalRequest oppdaterJournalRequest = oppdaterJournalRequestMapper.map(journalpost);
		oppdaterJournalRequest.setEndretAvNavn(RequestContextHolder.isRequestContextSet() ? RequestContextHolder.currentRequestContext().getUserId() : "srvbrevserver");
		verifyNotEmptyBruker(oppdaterJournalRequest);
		return oppdaterJournalRequest;
	}

	private void verifyNotEmptyBruker(OppdaterJournalRequest journalpost) {
		if (journalpost != null && (journalpost.getGjelderListe() == null || journalpost.getGjelderListe().isEmpty())) {
			log.error("OppdaterJournalpostRequest {} har ingen gyldige brukere etter oppdatering", journalpost.getJournalpostId());
		}
	}

	private void verifyNotEmptyBruker(Journalpost journalpost) {
		if (journalpost != null && (journalpost.getGjelderListe() == null || journalpost.getGjelderListe().isEmpty())) {
			log.error("Journalpost {} har ingen gyldige brukere før oppdatering", journalpost.getJournalpostId());
		}
	}

	private void verifyJournalStatus(Journalpost journalpost) throws BrevTechnicalException {
		for (String invalidJournalstatus : JOURNALSTATUS_LAGRE_INVALID_LIST) {
			if (journalpost.getJournalstatus().toString().equals(invalidJournalstatus)) {
				String msg = "Feil ved lagring/arkivering av dokument på journalpost med id '" + journalpost.getJournalpostId()
						+ "'. Journalstatus '" + journalpost.getJournalstatus()
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


	/**
	 * Extracts the filUUid from a Journalpost. If both fildetaljer with VariantFormat = ARKIV and VariantFormat = PRODUKSJON
	 * exists on the Journalpost, the filUUid of the first fildetaljer with VariantFormat = ARKIV is returned.
	 *
	 * @param journalpost The Journalpost on which to search for filUuid.
	 * @return The filUUid for the given Journalpost, or null if no filUUid is found.
	 */
	private String[] getFilUuid(Journalpost journalpost) throws BrevTechnicalException {
		no.nav.virksomhet.gjennomforing.arkiv.journal.v2.DokumentInfo dokInfo = journalpost.getJournalpostDokumentInfoRelasjonListe().iterator().next().getDokumentInfo();

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
