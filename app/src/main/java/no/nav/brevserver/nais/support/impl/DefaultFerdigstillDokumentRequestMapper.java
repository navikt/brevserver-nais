package no.nav.brevserver.nais.support.impl;


import no.nav.brevserver.nais.support.AbstractProviderDozerMapper;
import no.nav.brevserver.nais.support.FerdigstillDokumentRequestMapper;
import no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest;
import org.springframework.stereotype.Component;

/**
 * Default implementation of FerdigstillDokumentRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Component
public class DefaultFerdigstillDokumentRequestMapper extends AbstractProviderDozerMapper
		implements FerdigstillDokumentRequestMapper {

	@Override
	public FerdigstillDokumentRequest map(
			no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest ferdigstillDokumentRequest) {
		return getDozerMapper().map(ferdigstillDokumentRequest, FerdigstillDokumentRequest.class);
	}
}
