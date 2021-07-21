package no.nav.brevserver.fagarkiv.saf.hentdokument;

import lombok.Builder;
import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class HentDokumentResponseTo {

	private final byte[] dokument;

}
