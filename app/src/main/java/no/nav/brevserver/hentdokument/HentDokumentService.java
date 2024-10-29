package no.nav.brevserver.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.repository.BrevRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class HentDokumentService {

	private final BrevRepository brevRepository;

	public HentDokumentService(BrevRepository brevRepository) {
		this.brevRepository = brevRepository;
	}

	public Bilag hentDokumentFraBrevlager(String brevreferanse, String systemId) {
		log.info("Skal hente dokument fra Brevlager med brevreferanse={} og systemId={}", brevreferanse, systemId);

		Bilag dokument = getBrev(brevreferanse, systemId);

		log.info("Har hentet dokument fra Brevlager med brevreferanse={} og systemId={}", brevreferanse, systemId);
		return dokument;
	}

	private Bilag getBrev(String brevReferanse, String systemId) {
		var id = BrevreferanseSystemCompositeId.builder().systemId(systemId).brevreferanse(brevReferanse).build();
		Optional<Brev> brev = brevRepository.findById(id);

		return brev.map(Bilag::from).orElse(null);
	}
}
