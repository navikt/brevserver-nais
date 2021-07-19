package no.nav.brevserver.service.support;

import no.nav.brevserver.core.domain.entities.BrevSystemTilgang;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.domain.entities.Brevtilgang;
import no.nav.brevserver.core.repository.BrevSystemTilgangRepository;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.repository.BrevtilgangRepository;
import no.nav.brevserver.server.common.cache.CacheManager;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.SysTilgangVO;
import no.nav.brevserver.service.BrevserverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

@Service
@ComponentScan(basePackages = {
		"no.nav.brevserver.core.domain.entities"
})
public class DefaultBrevserverService implements BrevserverService {

	private BrevtilgangRepository brevtilgangRepository;

	private BrevstatusRepository brevstatusRepository;

	private BrevSystemTilgangRepository brevSystemTilgangRepository;

	@Autowired
	public DefaultBrevserverService(BrevtilgangRepository brevtilgangRepository,
									BrevstatusRepository brevstatusRepository,
									BrevSystemTilgangRepository brevSystemTilgangRepository) {
		this.brevtilgangRepository = brevtilgangRepository;
		this.brevstatusRepository = brevstatusRepository;
		this.brevSystemTilgangRepository = brevSystemTilgangRepository;
	}



	@Override
	public Brevstatus hentBrevStatus(String systemId, String brevreferanse) throws BrevTechnicalException {
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
		}
	}

}
