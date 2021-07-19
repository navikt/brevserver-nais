package no.nav.brevserver.nais.support;

import no.nav.brevserver.service.dokumentbehandling.to.FerdigstillDokumentRequest;

public interface FerdigstillDokumentRequestMapper {

	public FerdigstillDokumentRequest map(
			no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest ferdigstillDokumentRequest);
}
