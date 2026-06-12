package no.nav.brevserver.ws.dokumentbehandling.support.impl;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import no.nav.brevserver.ws.dokumentbehandling.support.HentDokumentResponseMapper;
import no.nav.brevserver.ws.dokumentbehandling.to.HentDokumentResponse;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import org.springframework.stereotype.Component;

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
