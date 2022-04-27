package no.nav.brevserver.service.dokumentbehandling.support.support;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.audit.AuditTrail;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import no.nav.brevserver.service.converter.BrevstatusTilVoConverter;
import no.nav.brevserver.service.converter.VoTilBrevstatusConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class DefaultBrevstatusService implements BrevstatusService {

	private final BrevstatusRepository brevstatusRepository;
	private final BrevtilgangService brevtilgangService;
	private final VoTilBrevstatusConverter voTilBrevstatusConverter;
	private final BrevstatusTilVoConverter brevstatusTilVoConverter;

	@Autowired
	public DefaultBrevstatusService(BrevstatusRepository brevstatusRepository,
									BrevtilgangService brevtilgangService,
									VoTilBrevstatusConverter voTilBrevstatusConverter,
									BrevstatusTilVoConverter brevstatusTilVoConverter) {
		this.brevstatusRepository = brevstatusRepository;
		this.brevtilgangService = brevtilgangService;
		this.voTilBrevstatusConverter = voTilBrevstatusConverter;
		this.brevstatusTilVoConverter = brevstatusTilVoConverter;
	}

	@Override
	public BrevStatusVO hentBrevStatus(String brevreferanse, String systemId) throws BrevTechnicalException {
		try {
			Optional<Brevstatus> brevstatusOpt = brevstatusRepository.findById(BrevreferanseSystemCompositeId.builder().brevreferanse(brevreferanse).systemId(systemId).build());
			if (brevstatusOpt.isEmpty()) {
				return null;
			} else {
				return brevstatusTilVoConverter.convert(brevstatusOpt.get());
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
	public BrevStatusVO lagreBrevStatus(BrevStatusVO brevStatusVO) throws BrevTechnicalException {

		String token = brevStatusVO.getToken();
		BrevStatusVO gmlStatus = null;
		try {
			// Sjekk om vi allerede har status.
			gmlStatus = hentBrevStatus(brevStatusVO.getBrevreferanse(), brevStatusVO.getSystemID());
			if (gmlStatus != null) {

				// vi har status, sjekk om det er noen felter som ikke er satt i brevStatus, legg inn gamle verdier hvis ikke
				if (brevStatusVO.getReturKoe() == null) {
					brevStatusVO.setReturKoe(gmlStatus.getReturKoe());
				}
				if (brevStatusVO.getBestillerBrukerID() == null) {
					brevStatusVO.setBestillerBrukerID(gmlStatus.getBestillerBrukerID());
				}
				if (brevStatusVO.getBrevmal() == null) {
					brevStatusVO.setBrevmal(gmlStatus.getBrevmal());
				}
				if (brevStatusVO.getArkiver() == null) {
					brevStatusVO.setArkiver(gmlStatus.getArkiver());
				}
				if (brevStatusVO.getFormat() == null) {
					brevStatusVO.setFormat(gmlStatus.getFormat());
				}
				if (brevStatusVO.getSkrivertype() == null) {
					brevStatusVO.setSkrivertype(gmlStatus.getSkrivertype());
				}
				if (brevStatusVO.getSkriver() == null) {
					brevStatusVO.setSkriver(gmlStatus.getSkriver());
				}
				if (brevStatusVO.getSkuff() == null) {
					brevStatusVO.setSkuff(gmlStatus.getSkuff());
				}
			}
			Brevstatus brevstatus = voTilBrevstatusConverter.convert(brevStatusVO);
			brevstatusRepository.save(brevstatus);

			if (token != null) {
				brevtilgangService.lagreTilgang(brevstatus.getId().getSystemId(), brevstatus.getId().getBrevreferanse(), token);
			}

		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG, e);

		}
		return gmlStatus;
	}
}
