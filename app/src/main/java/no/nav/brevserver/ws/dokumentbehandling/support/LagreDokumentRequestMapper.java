package no.nav.brevserver.ws.dokumentbehandling.support;


import no.nav.brevserver.ws.dokumentbehandling.to.LagreDokumentRequest;

public interface LagreDokumentRequestMapper {

	LagreDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest lagreDokumentRequest);
}
