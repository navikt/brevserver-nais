package no.nav.brevserver.ws.dokumentbehandling.support;

import no.nav.brevserver.ws.dokumentbehandling.to.FerdigstillDokumentRequest;

public interface FerdigstillDokumentRequestMapper {

	FerdigstillDokumentRequest map(
			no.nav.tjenester.brevogarkiv.dokumentbehandling.FerdigstillDokumentRequest ferdigstillDokumentRequest);
}
