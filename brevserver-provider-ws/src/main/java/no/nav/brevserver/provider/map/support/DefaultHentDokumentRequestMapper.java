package no.nav.brevserver.provider.map.support;

import no.nav.brevserver.provider.map.AbstractProviderDozerMapper;
import no.nav.brevserver.provider.map.HentDokumentRequestMapper;
import no.nav.brevserver.server.common.to.HentDokumentRequest;
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
