package no.nav.brevserver.service;

import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.repository.BrevlagerHistorikkRepository;
import no.nav.brevserver.service.converter.BrevTilBrevlagerHistorikkConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BrevlagerHistorikkServiceBean {

	private final BrevTilBrevlagerHistorikkConverter brevTilBrevlagerHistorikkConverter;
	private final BrevlagerHistorikkRepository brevlagerHistorikkRepository;

	@Autowired
	public BrevlagerHistorikkServiceBean(BrevlagerHistorikkRepository brevlagerHistorikkRepository, BrevTilBrevlagerHistorikkConverter brevTilBrevlagerHistorikkConverter){
		this.brevTilBrevlagerHistorikkConverter = brevTilBrevlagerHistorikkConverter;
		this.brevlagerHistorikkRepository = brevlagerHistorikkRepository;
	}


	public void insertHistorikk(Brev brev) {
		brevlagerHistorikkRepository.save(brevTilBrevlagerHistorikkConverter.convert(brev));
	}

}
