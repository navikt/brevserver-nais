package no.nav.brevserver.service.support;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.repository.BrevtilgangRepository;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultBrevstatusService implements BrevstatusService {

	private final BrevstatusRepository brevstatusRepository;
	private final BrevtilgangService brevtilgangService;

	@Autowired
	public DefaultBrevstatusService(BrevstatusRepository brevstatusRepository,
									BrevtilgangService brevtilgangService) {
		this.brevstatusRepository = brevstatusRepository;
		this.brevtilgangService = brevtilgangService;
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

	/**
	 * Lagrer brevstatus. Hvis brevstatus allerede eksistere blir den oppdatert. Hvis token er inkludert i brevstatusVO vil
	 * denne bli lagret
	 */

	@Override
	public Brevstatus lagreBrevStatus(Brevstatus brevStatus, String token) throws BrevTechnicalException {
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
				brevtilgangService.lagreTilgang(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), token);
			}

		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);

		}
		return gmlStatus;
	}
}
