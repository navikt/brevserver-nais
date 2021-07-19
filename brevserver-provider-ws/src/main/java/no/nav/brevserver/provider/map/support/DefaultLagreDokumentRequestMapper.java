package no.nav.brevserver.provider.map.support;

import no.nav.brevserver.provider.map.AbstractProviderDozerMapper;
import no.nav.brevserver.provider.map.LagreDokumentRequestMapper;
import no.nav.brevserver.server.common.to.LagreDokumentRequest;
import org.springframework.stereotype.Component;

/**
 * Default implementation of LagreDokumentRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Component
public class DefaultLagreDokumentRequestMapper extends AbstractProviderDozerMapper implements LagreDokumentRequestMapper {

	@Override
	public LagreDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest lagreDokumentRequest) {
		return getDozerMapper().map(lagreDokumentRequest, LagreDokumentRequest.class);
	}
}
