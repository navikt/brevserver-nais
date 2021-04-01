package no.nav.brevserver.provider.map.support;

import no.nav.brevserver.provider.map.AbstractProviderDozerMapper;
import no.nav.brevserver.provider.map.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest;

/**
 * Default implementation of FerdigstillDokumentRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultFerdigstillDokumentRequestMapper extends AbstractProviderDozerMapper
		implements FerdigstillDokumentRequestMapper {

	@Override
	public FerdigstillDokumentRequest map(
			no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest ferdigstillDokumentRequest) {
		return getDozerMapper().map(ferdigstillDokumentRequest, FerdigstillDokumentRequest.class);
	}
}
