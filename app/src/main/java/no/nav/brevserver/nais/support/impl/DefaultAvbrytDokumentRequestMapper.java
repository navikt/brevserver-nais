package no.nav.brevserver.nais.support.impl;

import no.nav.brevserver.nais.support.AbstractProviderDozerMapper;
import no.nav.brevserver.nais.support.AvbrytDokumentRequestMapper;
import no.nav.brevserver.service.dokumentbehandling.to.AvbrytDokumentRequest;
import org.springframework.stereotype.Component;

@Component
public class DefaultAvbrytDokumentRequestMapper extends AbstractProviderDozerMapper implements AvbrytDokumentRequestMapper {

	@Override
	public AvbrytDokumentRequest map(
			no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest avbrytDokumentRequest) {
		return getDozerMapper().map(avbrytDokumentRequest, AvbrytDokumentRequest.class);
	}
}
