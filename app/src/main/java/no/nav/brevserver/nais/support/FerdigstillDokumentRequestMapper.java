package no.nav.brevserver.nais.support;

import no.nav.brevserver.app.dokumentbehandling.to.FerdigstillDokumentRequest;

public interface FerdigstillDokumentRequestMapper {

	FerdigstillDokumentRequest map(
			no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest ferdigstillDokumentRequest);
}
