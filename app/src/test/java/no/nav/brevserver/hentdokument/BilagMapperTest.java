package no.nav.brevserver.hentdokument;

import no.nav.brevserver.core.domain.entities.Brev;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BilagMapperTest {

	@Test
	void skalMappeTilBilag() {
		var brev = new Brev();
		var brevdata = "Brevdata i bilaget".getBytes();
		var contentType = "PDF";
		brev.setBrevdata(brevdata);
		brev.setContentType(contentType);

		var bilag = BilagMapper.toBilag(brev);

		assertThat(bilag.brevdata()).isEqualTo(brevdata);
		assertThat(bilag.contentType()).isEqualTo("PDF");
	}
}