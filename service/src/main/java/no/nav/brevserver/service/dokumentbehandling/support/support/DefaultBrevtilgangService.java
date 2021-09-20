package no.nav.brevserver.service.dokumentbehandling.support.support;

import no.nav.brevserver.core.cache.LokalCacheConfig;
import no.nav.brevserver.core.domain.entities.BrevSystemTilgang;
import no.nav.brevserver.core.domain.entities.Brevtilgang;
import no.nav.brevserver.core.repository.BrevSystemTilgangRepository;
import no.nav.brevserver.core.repository.BrevtilgangRepository;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.SysTilgangVO;
import no.nav.brevserver.service.BrevtilgangService;
import no.nav.brevserver.service.converter.SystemtilgangTilVoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class DefaultBrevtilgangService implements BrevtilgangService {

	private final BrevtilgangRepository brevtilgangRepository;
	private final BrevSystemTilgangRepository brevSystemTilgangRepository;
	private final SystemtilgangTilVoConverter systemtilgangTilVoConverter;

	@Autowired
	public DefaultBrevtilgangService(BrevtilgangRepository brevtilgangRepository,
									 BrevSystemTilgangRepository brevSystemTilgangRepository,
									 SystemtilgangTilVoConverter systemtilgangTilVoConverter) {
		this.brevtilgangRepository = brevtilgangRepository;
		this.brevSystemTilgangRepository = brevSystemTilgangRepository;
		this.systemtilgangTilVoConverter = systemtilgangTilVoConverter;
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
	@Cacheable(cacheNames = LokalCacheConfig.SYSTEM_TILGANG_CACHE)
	public boolean sjekkSystemTilgang(String systemId, String passord) throws BrevTechnicalException {
		try {
			List<BrevSystemTilgang> systemTilgangList = brevSystemTilgangRepository.findBySysId(systemId);
			if (systemTilgangList.size() > 0) {
				return passord.equals(systemTilgangList.get(0).getPwd());
			}
			return false;
		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
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
				.opprettetDato(new Timestamp(System.currentTimeMillis()))
				.build();
		try {
			brevtilgangRepository.save(brevtilgang);
			return true;
		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		}
	}

	@Override
	@Cacheable(cacheNames = LokalCacheConfig.HENT_SYSTEM_TILGANG_CACHE)
	public SysTilgangVO hentTilgangMedCache(String systemId) throws BrevTechnicalException {
		return hentTilgangUtenCache(systemId);
	}

	@Override
	public SysTilgangVO hentTilgangUtenCache(String systemId) throws BrevTechnicalException {
		try {
			List<BrevSystemTilgang> systemTilgangList = brevSystemTilgangRepository.findBySysId(systemId);
			if (systemTilgangList.size() > 0) {
				return systemtilgangTilVoConverter.convert(systemTilgangList.get(0));
			}
			return null;
		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		}
	}
}
