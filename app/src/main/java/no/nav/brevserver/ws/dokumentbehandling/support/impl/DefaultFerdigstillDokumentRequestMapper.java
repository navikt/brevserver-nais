package no.nav.brevserver.ws.dokumentbehandling.support.impl;


import jakarta.activation.DataHandler;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.ws.dokumentbehandling.support.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.ws.dokumentbehandling.to.FerdigstillDokumentRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Default implementation of FerdigstillDokumentRequestMapper
 */
@Component
public class DefaultFerdigstillDokumentRequestMapper implements FerdigstillDokumentRequestMapper {

	@Override
	public FerdigstillDokumentRequest map(
			no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest ferdigstillDokumentRequest) {
		FerdigstillDokumentRequest mappedFerdigstillDokumentRequest = new FerdigstillDokumentRequest();
		mappedFerdigstillDokumentRequest.setNewDocument(ferdigstillDokumentRequest.isNyttDokument());
		mappedFerdigstillDokumentRequest.setBrevStatus(mapBrevStatus(ferdigstillDokumentRequest));
		mappedFerdigstillDokumentRequest.setBrev(mapBrev(ferdigstillDokumentRequest, ferdigstillDokumentRequest.getRedDokument()));
		mappedFerdigstillDokumentRequest.setPdfBrev(mapBrev(ferdigstillDokumentRequest, ferdigstillDokumentRequest.getPdfDokument()));
		return mappedFerdigstillDokumentRequest;
	}

	private BrevStatusVO mapBrevStatus(no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest ferdigstillDokumentRequest) {
		BrevStatusVO brevStatusVO = new BrevStatusVO();
		brevStatusVO.setSystemID(ferdigstillDokumentRequest.getSystemId());
		brevStatusVO.setBrevreferanse(ferdigstillDokumentRequest.getBrevreferanse());
		brevStatusVO.setToken(ferdigstillDokumentRequest.getToken());
		brevStatusVO.setBrevmal(ferdigstillDokumentRequest.getMalpakke());
		brevStatusVO.setReturKoe(ferdigstillDokumentRequest.getKvitteringskoe());
		return brevStatusVO;
	}

	private BrevVO mapBrev(no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest ferdigstillDokumentRequest, DataHandler dataHandler) {
		BrevVO brevVO = new BrevVO();
		brevVO.setSystemID(ferdigstillDokumentRequest.getSystemId());
		brevVO.setBrevreferanse(ferdigstillDokumentRequest.getBrevreferanse());
		brevVO.setBrukerID(ferdigstillDokumentRequest.getBrukerId());
		brevVO.setContentType(dataHandler == null ? null : dataHandler.getContentType());
		brevVO.setBrevdata(mapDokumentdata(dataHandler));
		return brevVO;
	}

	private byte[] mapDokumentdata(DataHandler dataHandler) {
		if (dataHandler == null) {
			return null;
		}
		try {
			return IOUtils.toByteArray(dataHandler.getInputStream());
		} catch (IOException e) {
			return null;
		}
	}
}
