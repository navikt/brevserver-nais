package no.nav.brevserver.nais.support;


import no.nav.brevserver.server.common.to.LagreDokumentRequest;

public interface LagreDokumentRequestMapper {

	LagreDokumentRequest map(no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest lagreDokumentRequest);
}
