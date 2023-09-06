package no.nav.brevserver.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.domain.entities.Brev;

@Slf4j
public class BilagMapper {

	public static Bilag toBilag(Brev brev) {
		return new Bilag(
				brev.getBrevdata(),
				brev.getContentType()
		);
	}

}
