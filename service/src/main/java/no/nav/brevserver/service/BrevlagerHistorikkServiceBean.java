package no.nav.brevserver.service;

import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.repository.BrevlagerHistorikkRepository;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.service.converter.BrevTilBrevlagerHistorikkConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

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
