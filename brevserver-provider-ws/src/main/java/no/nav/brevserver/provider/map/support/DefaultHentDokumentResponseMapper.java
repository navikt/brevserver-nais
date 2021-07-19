package no.nav.brevserver.provider.map.support;

import no.nav.brevserver.provider.map.AbstractProviderDozerMapper;
import no.nav.brevserver.provider.map.HentDokumentResponseMapper;
import no.nav.brevserver.server.common.to.HentDokumentResponse;
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
