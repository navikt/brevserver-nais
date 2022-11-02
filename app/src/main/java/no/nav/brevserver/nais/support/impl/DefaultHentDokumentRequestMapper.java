package no.nav.brevserver.nais.support.impl;

import no.nav.brevserver.app.dokumentbehandling.to.HentDokumentRequest;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.nais.support.HentDokumentRequestMapper;
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
