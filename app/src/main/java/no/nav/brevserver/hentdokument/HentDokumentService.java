package no.nav.brevserver.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.repository.BrevRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static no.nav.brevserver.hentdokument.HentDokumentController.OEBS_SYSTEMID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class HentDokumentService {

	private final BrevRepository brevRepository;

	public HentDokumentService(BrevRepository brevRepository) {
		this.brevRepository = brevRepository;
	}

	public Bilag hentDokumentFraBrevlager(String brevreferanse) {
		log.info("Skal hente dokument fra Brevlager med brevreferanse={}", brevreferanse);

		Bilag dokument = getBrev(brevreferanse);

		log.info("Har hentet dokument fra Brevlager med brevreferanse={}", brevreferanse);

		return dokument;
	}

	public Bilag getBrev(String brevReferanse) {
		var id = BrevreferanseSystemCompositeId.builder().systemId(OEBS_SYSTEMID).brevreferanse(brevReferanse).build();
		Optional<Brev> brev = brevRepository.findById(id);

		return brev.map(BilagMapper::toBilag).orElse(null);
	}

}
