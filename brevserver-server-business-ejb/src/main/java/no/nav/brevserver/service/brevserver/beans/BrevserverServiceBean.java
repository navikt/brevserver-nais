package no.nav.brevserver.service.brevserver.beans;

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
import no.nav.brevserver.service.brevserver.BrevserverService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementasjonen av brevserveren. Se metodebeskrivelsene for detaljer.
 */
public class BrevserverServiceBean implements BrevserverService {

	@Autowired
	private BrevtilgangRepository brevtilgangRepository;
	@Autowired
	private BrevstatusRepository brevstatusRepository;
	@Autowired
	private BrevSystemTilgangRepository brevSystemTilgangRepository;

	public BrevserverServiceBean() {
	}

	/**
	 * Lagrer token i t_brevtilgang
	 */
	@Transactional
	public boolean lagreTilgang(String systemId, String brevreferanse, String token) throws BrevTechnicalException {
		String methSig = "BrevserverServiceBean.lagreTilgang(" + brevreferanse + ")";

		Brevtilgang brevtilgang = Brevtilgang.builder()
				.brevreferanse(brevreferanse)
				.systemId(systemId)
				.token(token)
				.opprettetDato(LocalDateTime.now())
				.build();


		PerformanceLogger p = new PerformanceLogger(methSig);

		try {
			brevtilgangRepository.save(brevtilgang);
			return true;
		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		} finally {
			p.stop();
		}
	}

	/**
	 * Sjekker tilgang.
	 */
	public boolean sjekkTilgang(String systemId, String brevreferanse, String token) throws BrevTechnicalException {
		String methodSig = "BrevserverServiceBean.sjekkTilgang(" + brevreferanse + ")";
		boolean result = false;
		PerformanceLogger p = new PerformanceLogger(methodSig);
		try {
			List<Brevtilgang> tokenList = brevtilgangRepository.findBySystemIdAndBrevreferanse(systemId, brevreferanse);
			for (Brevtilgang brevtilgang : tokenList) {
				if (token != null && token.equals(brevtilgang.getToken())) {
					return true;
				}
			}
		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);
		} finally {
			p.stop();
		}
		return result;
	}

	/**
	 * Sjekker tilgang for saksbehandlingssystemer
	 */
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

	/**
	 * Lagrer brevstatus. Hvis brevstatus allerede eksistere blir den oppdatert. Hvis token er inkludert i brevstatusVO vil
	 * denne bli lagret
	 */
	public Brevstatus lagreBrevStatus(Brevstatus brevStatus, String token) throws BrevTechnicalException {
		String methSig = "BrevserverServiceBean.lagreBrevStatus(" + brevStatus.getBrevreferanse() + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);
		Brevstatus gmlStatus = null;
		try {
			// Sjekk om vi allerede har status.
			gmlStatus = hentBrevStatus(brevStatus.getSystemID(), brevStatus.getBrevreferanse());
			if (gmlStatus != null) {

				// vi har status, sjekk om det er noen felter som ikke er satt i brevStatus, legg inn gamle verdier hvis ikke
				if (brevStatus.getReturKoe() == null) {
					brevStatus.setReturKoe(gmlStatus.getReturKoe());
				}
				if (brevStatus.getBestillerBrukerID() == null) {
					brevStatus.setBestillerBrukerID(gmlStatus.getBestillerBrukerID());
				}
				if (brevStatus.getBrevmal() == null) {
					brevStatus.setBrevmal(gmlStatus.getBrevmal());
				}
				if (brevStatus.getArkiver() == null) {
					brevStatus.setArkiver(gmlStatus.getArkiver());
				}
				if (brevStatus.getFormat() == null) {
					brevStatus.setFormat(gmlStatus.getFormat());
				}
				if (brevStatus.getSkrivertype() == null) {
					brevStatus.setSkrivertype(gmlStatus.getSkrivertype());
				}
				if (brevStatus.getSkriver() == null) {
					brevStatus.setSkriver(gmlStatus.getSkriver());
				}
				if (brevStatus.getSkuff() == null) {
					brevStatus.setSkuff(gmlStatus.getSkuff());
				}
			}

			Brevstatus brevstatus = brevstatusRepository.save(brevStatus);
				//TODO: Fix default verdi
				//stmt.setString(10, getValueOrDefault(brevStatus.getArkiver(), "JA"));

			if (token != null) {
				lagreTilgang(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), token);
			}

		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);

		} finally {
			p.stop();
		}

		return gmlStatus;
	}

	private String getValueOrDefault(String value, String def) {
		if (value != null || "".equals(value)) {
			return value;
		} else {
			return def;
		}
	}

	public BrevSystemTilgang hentTilgang(String systemid, boolean useCache) throws BrevTechnicalException {
		String methSig = "BrevserverServiceBean.hentTilgang(" + systemid + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);

		SysTilgangVO result = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection con = null;

		try {
			// First check the cache
			if (useCache) {
				BrevSystemTilgang tmp = (BrevSystemTilgang) CacheManager.getObject(methSig);
				if (tmp != null) {
					return tmp;
				}
			}
			List<BrevSystemTilgang> brevSystemTilgangList = brevSystemTilgangRepository.findBySysId(systemid);
			if(brevSystemTilgangList.size()==0){
				return null;
			}
			BrevSystemTilgang brevSystemTilgang = brevSystemTilgangList.get(0);
			// Add to cache
			CacheManager.addObject(methSig, brevSystemTilgang);

			return brevSystemTilgang;
		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);

		} finally {
			p.stop();
		}
	}
}
