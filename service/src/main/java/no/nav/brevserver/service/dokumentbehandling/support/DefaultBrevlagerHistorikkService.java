package no.nav.brevserver.service.dokumentbehandling.support;

import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.repository.BrevlagerHistorikkRepository;
import no.nav.brevserver.service.BrevlagerHistorikkService;
import no.nav.brevserver.service.converter.BrevTilBrevlagerHistorikkConverter;
import org.springframework.stereotype.Service;

@Service
public class DefaultBrevlagerHistorikkService implements BrevlagerHistorikkService {

	private final BrevTilBrevlagerHistorikkConverter brevTilBrevlagerHistorikkConverter;
	private final BrevlagerHistorikkRepository brevlagerHistorikkRepository;

	public DefaultBrevlagerHistorikkService(BrevlagerHistorikkRepository brevlagerHistorikkRepository, BrevTilBrevlagerHistorikkConverter brevTilBrevlagerHistorikkConverter){
		this.brevTilBrevlagerHistorikkConverter = brevTilBrevlagerHistorikkConverter;
		this.brevlagerHistorikkRepository = brevlagerHistorikkRepository;
	}

	@Override
	public void insertHistorikk(Brev brev) {
		brevlagerHistorikkRepository.save(brevTilBrevlagerHistorikkConverter.convert(brev));
	}

}
