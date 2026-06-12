package no.nav.brevserver.ws.dokumentbehandling.support.impl;

import jakarta.activation.DataHandler;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.ws.dokumentbehandling.support.LagreDokumentRequestMapper;
import no.nav.brevserver.ws.dokumentbehandling.to.LagreDokumentRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DefaultLagreDokumentRequestMapper implements LagreDokumentRequestMapper {

	@Override
	public LagreDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest lagreDokumentRequest) {
		LagreDokumentRequest mappedLagreDokumentRequest = new LagreDokumentRequest();
		mappedLagreDokumentRequest.setNewDocument(lagreDokumentRequest.isNyttDokument());
		mappedLagreDokumentRequest.setBrevStatus(mapBrevStatus(lagreDokumentRequest));
		mappedLagreDokumentRequest.setBrev(mapBrev(lagreDokumentRequest));
		return mappedLagreDokumentRequest;
	}

	private BrevStatusVO mapBrevStatus(no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest lagreDokumentRequest) {
		BrevStatusVO brevStatusVO = new BrevStatusVO();
		brevStatusVO.setSystemID(lagreDokumentRequest.getSystemId());
		brevStatusVO.setBrevreferanse(lagreDokumentRequest.getBrevreferanse());
		brevStatusVO.setToken(lagreDokumentRequest.getToken());
		brevStatusVO.setBrevmal(lagreDokumentRequest.getMalpakke());
		brevStatusVO.setReturKoe(lagreDokumentRequest.getKvitteringskoe());
		return brevStatusVO;
	}

	private BrevVO mapBrev(no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest lagreDokumentRequest) {
		BrevVO brevVO = new BrevVO();
		brevVO.setSystemID(lagreDokumentRequest.getSystemId());
		brevVO.setBrevreferanse(lagreDokumentRequest.getBrevreferanse());
		DataHandler dataHandler = lagreDokumentRequest.getDokumentData();
		brevVO.setBrukerID(lagreDokumentRequest.getBrukerId());
		brevVO.setContentType(dataHandler == null ? null : dataHandler.getContentType());
		brevVO.setBrevdata(mapDokumentdata(dataHandler));
		return brevVO;
	}

	private byte[] mapDokumentdata(DataHandler dataHandler) {
		if(dataHandler == null) {
			return null;
		}
		try {
			return IOUtils.toByteArray(dataHandler.getInputStream());
		} catch (IOException e) {
			return null;
		}
	}
}
