package no.nav.brevserver.fagarkiv.dokarkiv.impl;

import no.nav.brevserver.dokarkiv.SafJournalpostQueryService;
import no.nav.brevserver.dokarkiv.journalpost.Journalpost;
import no.nav.brevserver.fagarkiv.dokarkiv.model.DokumentInfo;
import no.nav.brevserver.fagarkiv.dokarkiv.model.OppdaterJournalpostRequest;
import no.nav.brevserver.fagarkiv.mapper.OppdaterJournalRequestMapper;
import no.nav.brevserver.fagarkiv.reststs.StsRestConsumer;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevVO;
import org.springframework.stereotype.Service;

import static no.nav.brevserver.dokarkiv.constants.Constants.BEARER_PREFIX;

@Service
public class DokarkivService {

	static final String VARIANT_FORMAT_PRODUKSJON = "PRODUKSJON";
	static final String VARIANT_FORMAT_ARKIV = "ARKIV";
	static final String[] JOURNALSTATUS_LAGRE_INVALID_LIST = {"A", "FS", "FL"};

	private final SafJournalpostQueryService safJournalpostQueryService;
	private final DokarkivConsumerBean dokarkivConsumerBean;
	private final StsRestConsumer stsRestConsumer;
	private final OppdaterJournalRequestMapper oppdaterJournalRequestMapper;

	public DokarkivService(SafJournalpostQueryService safJournalpostQueryService,
						   DokarkivConsumerBean dokarkivConsumerBean,
						   StsRestConsumer stsRestConsumer,
						   OppdaterJournalRequestMapper oppdaterJournalRequestMapper) {
		this.safJournalpostQueryService = safJournalpostQueryService;
		this.dokarkivConsumerBean = dokarkivConsumerBean;
		this.stsRestConsumer = stsRestConsumer;
		this.oppdaterJournalRequestMapper = oppdaterJournalRequestMapper;
	}


	public void lagreDokument(String brevreferanse, String contentType, byte[] brevdata) throws BrevTechnicalException {
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createOppdaterJournalRequest(brevreferanse);
		//setBrevDataOnRequest(contentType, brevdata, oppdaterJournalpostRequest, brevreferanse);
		dokarkivConsumerBean.oppdaterJournalpost(oppdaterJournalpostRequest, brevreferanse);
	}

	public void lagreFerdigstiltDokument(String brevreferanse, BrevVO redBrevVO, BrevVO pdfBrevVO) throws BrevTechnicalException {
		OppdaterJournalpostRequest oppdaterJournalRequest = createOppdaterJournalRequest(brevreferanse);
		//setBrevDataOnRequest(redBrevVO.getContentType(), redBrevVO.getBrevdata(), oppdaterJournalRequest);
		//setBrevDataOnRequest(pdfBrevVO.getContentType(), pdfBrevVO.getBrevdata(), oppdaterJournalRequest);
		dokarkivConsumerBean.oppdaterJournalpost(oppdaterJournalRequest, brevreferanse);
	}

	private OppdaterJournalpostRequest createOppdaterJournalRequest(String brevreferanse) throws BrevTechnicalException {
		Journalpost journalpost = safJournalpostQueryService.hentJournalpost(brevreferanse, getAuthorizationHeader());
		verifyJournalStatus(journalpost, brevreferanse);
		OppdaterJournalpostRequest oppdaterJournalpostRequest = oppdaterJournalRequestMapper.convert(journalpost);
		//oppdaterJournalpostRequest.setEndretAvNavn(RequestContextHolder.currentRequestContext().getUserId());
		return oppdaterJournalpostRequest;
	}

	private void verifyJournalStatus(Journalpost journalpost, String brevreferanse) throws BrevTechnicalException {
		for (String invalidJournalstatus : JOURNALSTATUS_LAGRE_INVALID_LIST) {
			if (journalpost.getJournalstatus().equals(invalidJournalstatus)) {
				String msg = "Feil ved lagring/arkivering av dokument på journalpost med id '" + brevreferanse
						+ "'. Journalstatus '" + journalpost.getJournalstatus()
						+ "' tillater ikke lagring/arkivering";
				throw new BrevTechnicalException(BrevTechnicalException.UGYLDIG_JOURNALSTATUS, msg, new Exception(msg));
			}
		}
	}

	/*
	private void setBrevDataOnRequest(String contentType, byte[] brevData, OppdaterJournalpostRequest oppdaterJournalpostRequest, String brevreferanse)
			throws BrevTechnicalException {
		Fildetaljer fildetaljer;
		if (FilType.PDF.getContentType().equals(contentType)) {
			fildetaljer = findFildetaljer(oppdaterJournalpostRequest, VARIANT_FORMAT_ARKIV, FilType.PDF.getJoarkCode());
			if (fildetaljer != null) {
				fildetaljer.setFiltypeKode(FilType.PDFA.getJoarkCode());
			}
		} else if (FilType.RTF.getContentType().equals(contentType)) {
			fildetaljer = findFildetaljer(oppdaterJournalpostRequest, VARIANT_FORMAT_PRODUKSJON, FilType.RTF.getJoarkCode());
		} else if (FilType.DOCX.getContentType().equals(contentType)) {
			fildetaljer = findFildetaljer(oppdaterJournalpostRequest, VARIANT_FORMAT_PRODUKSJON, FilType.RTF.getJoarkCode());
			if (fildetaljer != null) {
				fildetaljer.setFiltypeKode(FilType.DOCX.getJoarkCode());
			} else {
				fildetaljer = findFildetaljer(oppdaterJournalpostRequest, VARIANT_FORMAT_PRODUKSJON, FilType.DOCX.getJoarkCode());
			}
		} else {
			throw new BrevTechnicalException("Ugyldig filType '" + contentType + "' mottatt, kan ikke lagre i JOARK.");
		}
		if (fildetaljer == null) {
			throw new BrevTechnicalException("Fant ikke filDetaljer for filtype " + contentType
					+ " på journalpost med brevreferanse " + brevreferanse);
		}
		fildetaljer.setFil(brevData);
	}


	private Fildetaljer findFildetaljer(OppdaterJournalpostRequest oppdaterJournalpostRequest, String variantFormat, String filtype, String brevreferanse)
			throws BrevTechnicalException {
		DokumentInfo dokumentInfo = getDokumentInfo(oppdaterJournalpostRequest, brevreferanse);

		for (Fildetaljer fildetaljer : dokumentInfo) {
			if (fildetaljer.getVariantFormatKode().equals(variantFormat) && fildetaljer.getFiltypeKode().equals(filtype)) {
				return fildetaljer;
			}
		}
		return null;
	}
	 */

	private DokumentInfo getDokumentInfo(OppdaterJournalpostRequest oppdaterJournalRequest, String brevreferanse) throws BrevTechnicalException {
		DokumentInfo dokumentInfo = null;
		if (oppdaterJournalRequest.getDokumenter()!=null && oppdaterJournalRequest.getDokumenter().length>0) {
			dokumentInfo = oppdaterJournalRequest.getDokumenter()[0];
		}
		if (dokumentInfo == null) {
			throw new BrevTechnicalException("Fant ikke JournalpostDokumentInfoRelasjon på journalpost med brevreferanse "
					+ brevreferanse);
		}
		if (dokumentInfo == null) {
			throw new BrevTechnicalException("Fant ikke DokumentInfo på journalpost med brevreferanse "
					+ brevreferanse);
		}
		return dokumentInfo;
	}

	private String getAuthorizationHeader() {
		return BEARER_PREFIX + stsRestConsumer.getOidcToken();
	}
}
