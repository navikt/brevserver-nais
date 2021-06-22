package no.nav.brevserver.service;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.repository.BrevstatusRepository;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrevstatusServiceBean {

	private final BrevstatusRepository brevstatusRepository;

	@Autowired
	public BrevstatusServiceBean(BrevstatusRepository brevstatusRepository) {
		this.brevstatusRepository = brevstatusRepository;
	}

	public Brevstatus hentBrevStatus(String systemId, String brevreferanse) throws BrevTechnicalException {
		String methodSig = "BrevserverServiceBean.hentBrevStatus(" + brevreferanse + ")";
		PerformanceLogger p = new PerformanceLogger(methodSig);

		try {
			List<Brevstatus> brevstatusList = brevstatusRepository.findByBrevreferanseAndSystemID(brevreferanse, systemId);
			if (brevstatusList.size() == 0) {
				return null;
			} else {
				return brevstatusList.get(0);
				//brevstatus.setKnappStatus(KnappStatusUtil.getKnappStatus(brevStatus.getBrevmal()));
			}
		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		} finally {
			p.stop();
		}
	}
}
