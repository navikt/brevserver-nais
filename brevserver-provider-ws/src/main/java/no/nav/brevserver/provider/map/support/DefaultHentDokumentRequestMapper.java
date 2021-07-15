package no.nav.brevserver.provider.map.support;

import no.nav.brevserver.provider.map.AbstractProviderDozerMapper;
import no.nav.brevserver.provider.map.HentDokumentRequestMapper;
import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentRequest;

/**
 * Default implementation of HentDokumentRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultHentDokumentRequestMapper extends AbstractProviderDozerMapper implements HentDokumentRequestMapper {

	@Override
	public HentDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentRequest hentDokumentRequest) {
		return getDozerMapper().map(hentDokumentRequest, HentDokumentRequest.class);
	}
}
