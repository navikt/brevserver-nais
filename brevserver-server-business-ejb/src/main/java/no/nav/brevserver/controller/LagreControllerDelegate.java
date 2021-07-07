package no.nav.brevserver.controller;

import no.nav.brevserver.consumer.joark.JoarkServiceBi;
import no.nav.brevserver.consumer.joark.factory.JoarkServiceBeanFactory;
import no.nav.brevserver.converter.VoTilBrevstatusConverter;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.ArgumentValidator;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.service.brevlager.BrevlagerService;
import no.nav.brevserver.service.brevlager.BrevlagerServiceFactory;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import no.nav.brevserver.service.jms.MessageProducer;
import no.nav.brevserver.service.jms.MessageProducerFactory;
import no.nav.brevserver.service.xml.XMLService;
import no.nav.brevserver.service.xml.XMLServiceFactory;

/**
 * Skriveoperasjoner p� brev.
 *
 * @author Marius Th�ring, Visma Consulting
 */
public class LagreControllerDelegate extends AbstractControllerDelegate {

	private VoTilBrevstatusConverter converter = new VoTilBrevstatusConverter();

	/**
	 * Refer to {@link ControllerBi#lagreDokument}
	 */
	public void lagreDokument(BrevVO brev, BrevStatusVO brevStatus, SystemType systemType)
			throws BrevException {
		ArgumentValidator.isNotNull(brev);
		ArgumentValidator.isNotNull(brevStatus);

		String methSig = "lagreDokument(" + brevStatus.getBrevreferanse() + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);
		try {
			verifyChangeRequest(brevStatus);
			if (brevStatus.getSystemID().startsWith(SystemType.PE.toString())) {
				lagreJoarkDokument(brev, brevStatus, systemType, brevStatus.getReturKoe());
			} else {
				lagreBrevlagerDokument(brev, brevStatus, systemType);
			}
			sendKvittering(brev, brevStatus, systemType, brevStatus.getReturKoe());
		} finally {
			p.stop();
		}
	}
	
	/**
	 * Refer to {@link ControllerBi#ferdigstillDokument}
	 */
	public void ferdigstillDokument(BrevStatusVO brevStatus, BrevVO redBrevVO, BrevVO pdfBrevVO, SystemType systemType)
			throws BrevException {
		ArgumentValidator.isNotNull(brevStatus);
		ArgumentValidator.isNotNull(redBrevVO);
		ArgumentValidator.isNotNull(pdfBrevVO);

		String methSig = "ferdigstillDokument(" + brevStatus.getBrevreferanse() + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);
		try {
			verifyChangeRequest(brevStatus);
			if (brevStatus.getSystemID().startsWith(SystemType.PE.toString())) {
				ferdigstillJoarkDokument(brevStatus, redBrevVO, pdfBrevVO);
			} else {
				ferdigstillBrevlagerDokument(brevStatus, redBrevVO, pdfBrevVO);
			}
			sendKvittering(pdfBrevVO, brevStatus, systemType, brevStatus.getReturKoe());
		} finally {
			p.stop();
		}
	}

	/**
	 * Refer to {@link ControllerBi#avbrytDokument}
	 */
	public void avbrytDokument(BrevStatusVO brevStatus) throws BrevException {
		ArgumentValidator.isNotNull(brevStatus);
		
		String methSig = "avbrytBrev(" + brevStatus.getBrevreferanse() + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);
		MessageProducer producer = MessageProducerFactory.getInstance().createMessageProducer(SystemType.BI);

		try {
			verifyChangeRequest(brevStatus);
			brevStatus.setStatus(Konstanter.BREVSTATUS_AVBRUTT);
			lagreDokumentStatus(brevStatus);

			if (brevStatus.getReturKoe() != null) {
				KvitteringVO kvittering = new KvitteringVO();
				kvittering.setSystemID(brevStatus.getSystemID());
				kvittering.setBrevreferanse(brevStatus.getBrevreferanse());
				brevStatus.setStatus(Konstanter.BREVSTATUS_AVBRUTT);

				XMLService service = XMLServiceFactory.getInstance().createXMLService();
				String xmlKvittering = service.unmarshal(kvittering, brevStatus);

				producer.sendReturMelding(brevStatus.getReturKoe(), false, null, xmlKvittering);
			}
			log.info(methSig, "Brevet ble avbrutt");
		} finally {
			p.stop();
		}
	}

	private void lagreJoarkDokument(BrevVO brev, BrevStatusVO brevstatus, SystemType systemType, String returKoe)
			throws BrevTechnicalException {
		JoarkServiceBi joarkService = JoarkServiceBeanFactory.getInstance().getJoarkService();
		joarkService.lagreDokument(brevstatus.getBrevreferanse(), brev.getContentType(), brev.getBrevdata());
	}

	private void lagreBrevlagerDokument(BrevVO brev, BrevStatusVO brevStatusVO, SystemType systemType)
			throws BrevTechnicalException {
		BrevlagerService service = BrevlagerServiceFactory.getInstance().createBrevlagerService();
		Brevstatus brevstatus = converter.convert(brevStatusVO);
		service.lagreBrev(brev, brevstatus,  brevStatusVO.getToken());
	}

	private void ferdigstillJoarkDokument(BrevStatusVO brevStatusVO, BrevVO redBrevVO, BrevVO pdfBrevVO)
			throws BrevTechnicalException {
		Brevstatus brevstatus = converter.convert(brevStatusVO);
		BrevserverServiceFactory.getInstance().createBrevserverService().lagreBrevStatus(brevstatus, brevStatusVO.getToken());
		JoarkServiceBi joarkService = JoarkServiceBeanFactory.getInstance().getJoarkService();
		joarkService.lagreFerdigstiltDokument(brevstatus.getBrevreferanse(), redBrevVO, pdfBrevVO);
	}

	private void ferdigstillBrevlagerDokument(BrevStatusVO brevstatus, BrevVO redBrevVO, BrevVO pdfBrevVO)
			throws BrevTechnicalException {
		BrevlagerService brevlagerService = BrevlagerServiceFactory.getInstance().createBrevlagerService();
		brevlagerService.ferdigstillBrev(converter.convert(brevstatus), redBrevVO, pdfBrevVO, brevstatus.getToken());
	}

	private void sendKvittering(BrevVO brev, BrevStatusVO brevstatus, SystemType systemType, String returKoe)
			throws BrevTechnicalException {
		String methSig = "sendKvittering(" + brev.getBrevreferanse() + ")";
		log.info(methSig, "Sender kvittering, brevStatus: " + brev.getLagerStatus());

		KvitteringVO kvittering = createKvittering(brevstatus, brev);
		XMLService xmlService = XMLServiceFactory.getInstance().createXMLService();
		String xmlKvittering = xmlService.unmarshal(kvittering, brevstatus);

		MessageProducer producer = MessageProducerFactory.getInstance().createMessageProducer(systemType);
		producer.sendReturMelding(returKoe, false, null, xmlKvittering);
	}

	private KvitteringVO createKvittering(BrevStatusVO brevstatus, BrevVO brev) {
		KvitteringVO kvittering = new KvitteringVO();
		kvittering.setBrevreferanse(brevstatus.getBrevreferanse());
		kvittering.setSystemID(brevstatus.getSystemID());
		kvittering.setLagerStatus(brevstatus.getStatus());
		kvittering.setContentType(brev.getContentType());
		return kvittering;
	}
}
