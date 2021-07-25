package no.nav.brevserver.nais.support.impl;

import no.nav.brevserver.nais.support.AbstractProviderDozerMapper;
import no.nav.brevserver.nais.support.HentDokumentRequestMapper;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentRequest;
import org.springframework.stereotype.Component;

/**
 * Default implementation of HentDokumentRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Component
public class DefaultHentDokumentRequestMapper extends AbstractProviderDozerMapper implements HentDokumentRequestMapper {

	@Override
	public HentDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest hentDokumentRequest) {
		return getDozerMapper().map(hentDokumentRequest, HentDokumentRequest.class);
	}
}
