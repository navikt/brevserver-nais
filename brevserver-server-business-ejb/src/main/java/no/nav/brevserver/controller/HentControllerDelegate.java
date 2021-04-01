package no.nav.brevserver.controller;

import no.nav.brevserver.consumer.joark.JoarkServiceBi;
import no.nav.brevserver.consumer.joark.factory.JoarkServiceBeanFactory;
import no.nav.brevserver.server.common.config.KnappStatus;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.ArgumentValidator;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.service.brevlager.BrevlagerService;
import no.nav.brevserver.service.brevlager.BrevlagerServiceFactory;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import no.nav.brevserver.service.converter.FileConverter;

/**
 * Leseoperasjoner på brev.
 *
 * @author Marius Thøring, Visma Consulting
 */
public class HentControllerDelegate extends AbstractControllerDelegate {

    private static String BREV_STATUS_KASSERT = "KASSERT";
    private static String LAGER_STATUS_A = "A";

    /**
     * Refer to {@link ControllerBi#hentDokument}
     */
    public BrevVO hentDokument(BrevStatusVO brevStatus) throws BrevException {
        ArgumentValidator.isNotNull(brevStatus);

        String methSig = "getBrev(" + brevStatus.getBrevreferanse() + ")";
        PerformanceLogger p = new PerformanceLogger(methSig);
        BrevVO result;

        try {
            checkRequiredFields(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken());
            sjekkSystemTokenTilgang(brevStatus.getSystemID(), brevStatus.getBrevreferanse(), brevStatus.getToken());
            if (brevStatus.getSystemID().startsWith(SystemType.PE.toString())) {
                result = hentDokumentFraJOARK(brevStatus.getBrevreferanse());
            } else {
                result = hentDokumentFraBrevlager(brevStatus.getSystemID(), brevStatus.getBrevreferanse());
            }
        } finally {
            p.stop();
        }

        return result;
    }

    /**
     * Refer to {@link ControllerBi#hentDokumentStatus}
     */
    public KnappStatus hentKnappStatus(String systemId, String brevreferanse) throws BrevException {
        String methSig = "BrevEJB.hentKnappStatus(" + brevreferanse + ")";
        BrevStatusVO result = null;
        PerformanceLogger p = new PerformanceLogger(methSig);
        try {
            result = BrevserverServiceFactory.getInstance().createBrevserverService()
                    .hentBrevStatus(systemId, brevreferanse);
            if (result == null) {
                return KnappStatus.getDefault();
            }
        } finally {
            p.stop();
        }
        return result.getKnappStatus();
    }


    private BrevVO hentDokumentFraJOARK(String brevreferanse) throws BrevTechnicalException, BrevFunctionalException {
        JoarkServiceBi joarkService = JoarkServiceBeanFactory.getInstance().getJoarkService();
        BrevVO result = joarkService.hentDokument(brevreferanse);

        if (LAGER_STATUS_A.equals(result.getLagerStatus()) && result.getContentType().equals(FilType.RTF.getContentType())) {
            konverterRtfTilPdf(result, brevreferanse);
        }
        return result;
    }

    private void konverterRtfTilPdf(BrevVO result, String brevreferanse) throws BrevTechnicalException {
        try {
            result.setBrevdata(FileConverter.getInstance().convertToPdf(result.getBrevdata()));
            result.setContentType(FilType.PDF.getContentType());
        } catch (Exception e) {
            throw new BrevTechnicalException("Greide ikke å konvertere dokument med brevreferanse " + brevreferanse
                    + " til pdf", e);
        }
    }

    private BrevVO hentDokumentFraBrevlager(String systemId, String brevreferanse) throws BrevTechnicalException, BrevFunctionalException {
        BrevlagerService brevlager = BrevlagerServiceFactory.getInstance().createBrevlagerService();
        BrevVO result = brevlager.getBrev(systemId, brevreferanse);

        if (result == null) {
            throw new BrevFunctionalException(BrevFunctionalException.FANT_IKKE_DOKUMENT, "Fant ikke dokumentet i Brevlageret");
        }

        if (BREV_STATUS_KASSERT.equals(result.getLagerStatus())) {
            String message = String.format("Dokumentet er flagget for kassasjon og vil bli slettet innen 3 maaneder, brevreferanse=%s, systemid=%s",
                    result.getBrevreferanse(), result.getSystemID());
            log.warning("hentDokumentFraBrevlager(" + systemId + ", " + brevreferanse + ")", message);
            throw new BrevFunctionalException(BrevFunctionalException.DOKUMENTET_ER_FLAGGET_FOR_KASSASJON, message);
        }

        return result;
    }
}
