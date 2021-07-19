package no.nav.brevserver.provider.map.support;

import no.nav.brevserver.provider.map.AbstractProviderDozerMapper;
import no.nav.brevserver.provider.map.AvbrytDokumentRequestMapper;
import no.nav.brevserver.server.common.to.AvbrytDokumentRequest;
import org.springframework.stereotype.Component;

/**
 * Default implementation of AvbrytDokumentRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Component
public class DefaultAvbrytDokumentRequestMapper extends AbstractProviderDozerMapper
		implements AvbrytDokumentRequestMapper {

	@Override
	public AvbrytDokumentRequest map(
			no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest avbrytDokumentRequest) {
		return getDozerMapper().map(avbrytDokumentRequest, AvbrytDokumentRequest.class);
	}
}
