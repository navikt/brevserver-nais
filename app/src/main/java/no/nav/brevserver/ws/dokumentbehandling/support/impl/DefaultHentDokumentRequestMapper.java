package no.nav.brevserver.ws.dokumentbehandling.support.impl;

import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.ws.dokumentbehandling.support.HentDokumentRequestMapper;
import no.nav.brevserver.ws.dokumentbehandling.to.HentDokumentRequest;
import org.springframework.stereotype.Component;

/**
 * Default implementation of HentDokumentRequestMapper
 */
@Component
public class DefaultHentDokumentRequestMapper implements HentDokumentRequestMapper {

	@Override
	public HentDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest hentDokumentRequest) {
		HentDokumentRequest mappedHentDokumentRequest = new HentDokumentRequest();
		BrevStatusVO brevStatus = new BrevStatusVO();
		brevStatus.setSystemID(hentDokumentRequest.getSystemId());
		brevStatus.setBrevreferanse(hentDokumentRequest.getBrevreferanse());
		brevStatus.setToken(hentDokumentRequest.getToken());
		mappedHentDokumentRequest.setBrevStatus(brevStatus);
		return mappedHentDokumentRequest;
	}
}
