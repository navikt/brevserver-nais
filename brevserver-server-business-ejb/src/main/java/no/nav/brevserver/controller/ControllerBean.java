package no.nav.brevserver.controller;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import no.nav.brevserver.server.common.config.KnappStatus;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevRuntimeException;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;

/**
 * EJB 3 implementation of {@link ControllerBi}.
 *
 * @author Marius Thøring, Visma Consulting
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class ControllerBean implements ControllerBi {

	private LagreControllerDelegate lagreControllerDelegate;
	private HentControllerDelegate hentControllerDelegate;

	public ControllerBean() {
		initDelegates();
	}

	@PostConstruct
	public void initDelegates() {
		hentControllerDelegate = new HentControllerDelegate();
		hentControllerDelegate.setLog(new Log(hentControllerDelegate.getClass()));
		lagreControllerDelegate = new LagreControllerDelegate();
		lagreControllerDelegate.setLog(new Log(lagreControllerDelegate.getClass()));
	}

	@Override
	public KnappStatus hentKnappStatus(String systemId, String brevreferanse) {
		try {
			return hentControllerDelegate.hentKnappStatus(systemId, brevreferanse);
		} catch (BrevException e) {
			throw new BrevRuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public BrevVO hentDokument(BrevStatusVO brevStatus) {
		try {
			return hentControllerDelegate.hentDokument(brevStatus);
		} catch (BrevException e) {
			throw new BrevRuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void lagreDokumentStatus(BrevStatusVO brevStatus) {
		try {
			lagreControllerDelegate.lagreDokumentStatus(brevStatus);
		} catch (BrevException e) {
			throw new BrevRuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void lagreDokument(BrevVO brev, BrevStatusVO brevstatus, SystemType systemType) {
		try {
			lagreControllerDelegate.lagreDokument(brev, brevstatus, systemType);
		} catch (BrevException e) {
			throw new BrevRuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void ferdigstillDokument(BrevStatusVO brevStatus, BrevVO redBrevVO, BrevVO pdfBrevVO, SystemType systemType) {
		try {
			lagreControllerDelegate.ferdigstillDokument(brevStatus, redBrevVO, pdfBrevVO, systemType);
		} catch (BrevException e) {
			throw new BrevRuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void avbrytDokument(BrevStatusVO brevStatus) {
		try {
			lagreControllerDelegate.avbrytDokument(brevStatus);
		} catch (BrevException e) {
			throw new BrevRuntimeException(e.getMessage(), e);
		}
	}
}
