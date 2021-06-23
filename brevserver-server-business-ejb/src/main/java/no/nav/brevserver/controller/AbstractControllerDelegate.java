package no.nav.brevserver.controller;

import no.nav.brevserver.converter.VoTilBrevstatusConverter;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevSecurityException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.utility.ArgumentValidator;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;

/**
 * Base class for {@link ControllerBi} delegates, contains common functionality.
 * 
 * @author Marius Thøring, Visma Consulting
 */
public abstract class AbstractControllerDelegate {
	protected Log log = null;
	private VoTilBrevstatusConverter converter = new VoTilBrevstatusConverter();

	/**
	 * Refer to {@link ControllerBi#lagreDokumentStatus}
	 */
	public void lagreDokumentStatus(BrevStatusVO brevStatus) throws BrevException {
		ArgumentValidator.isNotNull(brevStatus);

		String methSig = "BrevEJB.lagreBrevStatus(" + brevStatus.getBrevreferanse() + ", " + brevStatus.getStatus() + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);

		try {
			BrevserverServiceFactory.getInstance().createBrevserverService().lagreBrevStatus(converter.convert(brevStatus), brevStatus.getToken());
		} finally {
			p.stop();
		}
	}
	
	protected void verifyChangeRequest(BrevStatusVO brevStatus) throws BrevException {
		checkRequiredFields(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken());
		Brevstatus oldBrevStatus = BrevserverServiceFactory.getInstance().createBrevserverService()
				.hentBrevStatus(brevStatus.getSystemID(), brevStatus.getBrevreferanse());
		if (oldBrevStatus != null) {
			sjekkSystemTokenTilgang(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken());
			verifyEditableStatus(oldBrevStatus);
		}
	}

	private void verifyEditableStatus(Brevstatus brevStatus) throws BrevException {
		if (Konstanter.BREVSTATUS_FERDIG.equals(brevStatus.getStatus()) 
				|| Konstanter.BREVSTATUS_UTSKRIFT.equals(brevStatus.getStatus())) {
			throw new BrevFunctionalException("Brevet med brevreferanse " + brevStatus.getBrevreferanse() + " har status " + brevStatus.getStatus() + " og kan ikke endres");
		}
	}

	protected void sjekkSystemTokenTilgang(String systemId, String brevreferanse, String token) 
			throws BrevSecurityException, BrevTechnicalException {
		if (!BrevserverServiceFactory.getInstance().createBrevserverService().sjekkTilgang(systemId, brevreferanse, token)) {
			throw new BrevSecurityException("Tilgang til dokumentet avslått", BrevSecurityException.IKKE_TILGANG_I_BREVSERVER);
		}
	}

	protected void checkRequiredFields(String systemId, String brevreferanse, String token) throws BrevTechnicalException {
		if (systemId == null) {
			throw new BrevTechnicalException("Manglende obligatorisk felt: systemId");
		} else if (brevreferanse == null) {
			throw new BrevTechnicalException("Manglende obligatorisk felt: brevreferanse");
		} else if (token == null) {
			throw new BrevTechnicalException("Manglende obligatorisk felt: token");
		}
	}

	public void setLog(Log log) {
		this.log = log;
	}
}
