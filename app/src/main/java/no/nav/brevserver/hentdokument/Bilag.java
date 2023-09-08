package no.nav.brevserver.hentdokument;

import no.nav.brevserver.core.domain.entities.Brev;

public record Bilag(
		byte[] brevdata,
		String contentType
) {

	public static Bilag from(Brev brev) {
		return new Bilag(
				brev.getBrevdata(),
				brev.getContentType()
		);
	}
}
