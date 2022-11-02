package no.nav.brevserver.nais.support.impl;

import no.nav.brevserver.app.dokumentbehandling.to.HentDokumentResponse;
import no.nav.brevserver.nais.support.HentDokumentResponseMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

/**
 * Default implementation of HentDokumentResponseMapper
 */
@Component
public class DefaultHentDokumentResponseMapper implements HentDokumentResponseMapper {

	@Override
	public HentDokumentResponse2 map(HentDokumentResponse hentDokumentResponse) {
		HentDokumentResponse2 mappedResponse = new HentDokumentResponse2();
		mappedResponse.setKnappStatus(hentDokumentResponse.getKnappStatus());
		mappedResponse.setDokumentData(new DataHandler(new ByteArrayDataSource(hentDokumentResponse.getDokumentData(), hentDokumentResponse.getContentType())));
		return mappedResponse;
	}
}
