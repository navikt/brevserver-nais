package no.nav.brevserver.provider.map.converters;

import no.nav.brevserver.service.dokumentbehandling.to.HentDokumentResponse;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;
import org.dozer.DozerConverter;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

/**
 * Converts between domain HentDokumentResponse and webservice HentDokumentResponse2
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class HentDokumentResponseCustomConverter extends DozerConverter<HentDokumentResponse, HentDokumentResponse2> {

	public HentDokumentResponseCustomConverter() {
		super(HentDokumentResponse.class, HentDokumentResponse2.class);
	}

	@Override
	public HentDokumentResponse2 convertTo(HentDokumentResponse source, HentDokumentResponse2 destination) {
		if (source == null) {
			return null;
		}

		HentDokumentResponse2 response = new HentDokumentResponse2();
		response.setKnappStatus(source.getKnappStatus());
		response.setDokumentData(new DataHandler(new ByteArrayDataSource(source.getDokumentData(), source.getContentType())));
		return response;
	}

	@Override
	public HentDokumentResponse convertFrom(HentDokumentResponse2 source, HentDokumentResponse destination) {
		throw new UnsupportedOperationException("Convert HentDokumentResponse from WS to domain is not supported");
	}
}
