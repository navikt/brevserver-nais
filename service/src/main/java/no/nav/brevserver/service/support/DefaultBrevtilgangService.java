package no.nav.brevserver.service.support;

import no.nav.brevserver.core.domain.entities.BrevSystemTilgang;
import no.nav.brevserver.core.domain.entities.Brevtilgang;
import no.nav.brevserver.core.repository.BrevSystemTilgangRepository;
import no.nav.brevserver.core.repository.BrevtilgangRepository;
import no.nav.brevserver.server.common.cache.CacheManager;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.service.BrevtilgangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DefaultBrevtilgangService implements BrevtilgangService {

	private final BrevtilgangRepository brevtilgangRepository;
	private final BrevSystemTilgangRepository brevSystemTilgangRepository;

	@Autowired
	public DefaultBrevtilgangService(BrevtilgangRepository brevtilgangRepository,
									 BrevSystemTilgangRepository brevSystemTilgangRepository) {
		this.brevtilgangRepository = brevtilgangRepository;
		this.brevSystemTilgangRepository = brevSystemTilgangRepository;
	}

	/**
	 * Sjekker tilgang.
	 */
	@Override
	public boolean sjekkTilgang(String systemId, String brevreferanse, String token) throws BrevTechnicalException {
		boolean result = false;
		try {
			List<Brevtilgang> tokenList = brevtilgangRepository.findBySystemIdAndBrevreferanse(systemId, brevreferanse);
			for (Brevtilgang brevtilgang : tokenList) {
				if (token != null && token.equals(brevtilgang.getToken())) {
					return true;
				}
			}
		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		}
		return result;
	}

	/**
	 * Sjekker tilgang for saksbehandlingssystemer
	 */
	@Override
	public boolean sjekkSystemTilgang(String systemId, String passord) throws BrevTechnicalException {
		String methodSig = "BrevserverServiceBean.sjekkSystemTilgang(" + systemId + ")";
		PerformanceLogger p = new PerformanceLogger(methodSig);
		String passordCached = (String) CacheManager.getObject(methodSig);
		if (passordCached != null) {
			return passordCached.equals(passord);
		} else {
			try {
				List<BrevSystemTilgang> brevSystemTilgangList = brevSystemTilgangRepository.findBySysId(systemId);

				if (brevSystemTilgangList!=null&&brevSystemTilgangList.size()>0) {
					String syspassord = brevSystemTilgangList.get(0).getPwd();
					CacheManager.addObject(methodSig, syspassord);
					return syspassord.equals(passord);
				} else {
					return false;
				}
			} catch (Exception e) {
				throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
			} finally {
				p.stop();
			}
		}
	}

	/**
	 * Lagrer token i t_brevtilgang
	 */
	@Override
	public boolean lagreTilgang(String systemId, String brevreferanse, String token) throws BrevTechnicalException {
		Brevtilgang brevtilgang = Brevtilgang.builder()
				.brevreferanse(brevreferanse)
				.systemId(systemId)
				.token(token)
				.opprettetDato(LocalDateTime.now())
				.build();
		try {
			brevtilgangRepository.save(brevtilgang);
			return true;
		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		}
	}

}
