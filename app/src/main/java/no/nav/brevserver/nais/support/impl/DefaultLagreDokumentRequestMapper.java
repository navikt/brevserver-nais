package no.nav.brevserver.nais.support.impl;

import no.nav.brevserver.nais.support.AbstractProviderDozerMapper;
import no.nav.brevserver.nais.support.LagreDokumentRequestMapper;
import no.nav.brevserver.app.dokumentbehandling.to.LagreDokumentRequest;
import org.springframework.stereotype.Component;

@Component
public class DefaultLagreDokumentRequestMapper extends AbstractProviderDozerMapper implements LagreDokumentRequestMapper {

	@Override
	public LagreDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest lagreDokumentRequest) {
		return getDozerMapper().map(lagreDokumentRequest, LagreDokumentRequest.class);
	}
}
