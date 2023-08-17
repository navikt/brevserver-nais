package no.nav.brevserver.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.domain.entities.Brev;

import static no.nav.brevserver.core.constants.Konstanter.CONTENTTYPE_DOCX_SHORT;
import static no.nav.brevserver.core.vo.FilType.DOCX;

@Slf4j
public class BilagMapper {

	public static Bilag toBilag(Brev brev) {
		return new Bilag(
				brev.getBrevdata(),
				translateContentTypeDocxFromDb2(brev.getContentType())
		);
	}

	private static String translateContentTypeDocxFromDb2(String contentType) {
		if (contentType != null && contentType.equals(CONTENTTYPE_DOCX_SHORT)) {
			return DOCX.getContentType();
		}

		return contentType;
	}
}
