package no.nav.brevserver.ws.dokumentbehandling.support.impl;

import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.ws.dokumentbehandling.support.AvbrytDokumentRequestMapper;
import no.nav.brevserver.ws.dokumentbehandling.to.AvbrytDokumentRequest;
import org.springframework.stereotype.Component;

@Component
public class DefaultAvbrytDokumentRequestMapper implements AvbrytDokumentRequestMapper {

	@Override
	public AvbrytDokumentRequest map(
			no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest avbrytDokumentRequest) {
		AvbrytDokumentRequest mappedAvbrytDokumentRequest = new AvbrytDokumentRequest();
		BrevStatusVO brevStatus = new BrevStatusVO();
		brevStatus.setSystemID(avbrytDokumentRequest.getSystemId());
		brevStatus.setBrevreferanse(avbrytDokumentRequest.getBrevreferanse());
		brevStatus.setToken(avbrytDokumentRequest.getToken());
		mappedAvbrytDokumentRequest.setBrevStatus(brevStatus);
		return mappedAvbrytDokumentRequest;
	}
}
