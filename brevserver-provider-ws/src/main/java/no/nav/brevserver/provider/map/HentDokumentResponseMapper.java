package no.nav.brevserver.provider.map;

import no.nav.brevserver.server.common.to.HentDokumentResponse;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.HentDokumentResponse2;

/**
 * Interface for mapping between webservice and domain hentDokument response
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface HentDokumentResponseMapper {
	/**
	 * Maps from webservice domain to webservice response for hentDokument
	 *
	 * @param hentDokumentResponse The domain response
	 * @return The mapped webservice request
	 */
	HentDokumentResponse2 map(HentDokumentResponse hentDokumentResponse);
}
