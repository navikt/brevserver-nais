package no.nav.brevserver.controller;

import no.nav.brevserver.server.common.config.KnappStatus;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;

/**
 * Interface som definerer tilgjengelige operasjoner for servletene (Brevklient).
 *
 * @author Marius Thåring, Visma Consulting
 */
public interface ControllerBi {

	/**
	 * Henter aktive knapper for angitt brev.
	 *
	 * @param systemId
	 * @param brevreferanse
	 * @return aktive knapper for brevmal
	 * @throws BrevException Hvis en feil oppstår
	 */
	KnappStatus hentKnappStatus(String systemId, String brevreferanse);

	/**
	 * Lagrer status for angitt brev.
	 *
	 * @param brevStatus Status på brev
	 * @throws BrevException Hvis en feil oppstår
	 */
	void lagreDokumentStatus(BrevStatusVO brevStatus);

	/**
	 * Henter brev.
	 *
	 * @param brevStatus Status på brev
	 * @return BrevVO (brev og metadata)
	 * @throws BrevException Hvis en feil oppstår
	 */
	BrevVO hentDokument(BrevStatusVO brevStatus);

	/**
	 * Mellomlagrer brev.
	 *
	 * @param brev       Brev og metadata
	 * @param brevstatus Status på brev
	 * @param systemType Type system
	 * @throws BrevException Hvis en feil oppstår
	 */
	void lagreDokument(BrevVO brev, BrevStatusVO brevstatus, SystemType systemType);

	/**
	 * Ferdigstiller brev og lagrer både kladd og ferdigstilt brev.
	 *
	 * @param brevStatus Status på brev
	 * @param redBrevVO  Redigerbart brev og metadata
	 * @param pdfBrevVO  Ferdigstilt brev og metadata
	 * @param systemType Type system
	 * @throws BrevException Hvis en feil oppstår
	 */
	void ferdigstillDokument(BrevStatusVO brevStatus, BrevVO redBrevVO, BrevVO pdfBrevVO, SystemType systemType);

	/**
	 * Avbryter et brev vedåsette en avbrutt status.
	 *
	 * @param brevStatus Status på brev
	 * @throws BrevException Hvis en feil oppstår
	 */
	void avbrytDokument(BrevStatusVO brevStatus);

}
