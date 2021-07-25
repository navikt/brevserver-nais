package no.nav.brevserver.nais.support.impl;

import no.nav.brevserver.nais.support.AbstractProviderDozerMapper;
import no.nav.brevserver.nais.support.HentDokumentResponseMapper;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentResponse;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import org.springframework.stereotype.Component;

/**
 * Default implementation of HentDokumentResponseMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Component
public class DefaultHentDokumentResponseMapper extends AbstractProviderDozerMapper implements HentDokumentResponseMapper {

	@Override
	public HentDokumentResponse2 map(HentDokumentResponse hentDokumentResponse) {
		return getDozerMapper().map(hentDokumentResponse, HentDokumentResponse2.class);
	}
}
