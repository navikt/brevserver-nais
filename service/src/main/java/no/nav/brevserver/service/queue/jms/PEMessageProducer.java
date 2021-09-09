package no.nav.brevserver.service.queue.jms;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.jms.JMSAccessor;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.utility.ArgumentValidator;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.queue.xml.XMLService;
import no.nav.brevserver.service.queue.xml.XMLServiceFactory;
import org.springframework.stereotype.Component;

/**
 * Produserer JMS tekstmeldinger, fortrinnsvis i XML.
 */
@Component
@Slf4j
public class PEMessageProducer extends MessageProducer {

	public PEMessageProducer() {
	}

	/**
	 * Sender en bestilling til Dialogue
	 *
	 * @param message meldingen er direkte kopiert fra MessageConsumer
	 * @throws BrevTechnicalException ved alle feil
	 */
	public void sendToDialogue(MessageVO message) throws BrevTechnicalException {
		//produserTextMelding(Konstanter.KONF_SEND_DIALOGUE_ONLINE_PE, true, null, message.getCorrelationID(),
		//		message.getStringBody());
	}

	/**
	 * Sender en melding til deadletter køen
	 *
	 * @param msgVO MeldingsVO Inneholder meldingsinformasjon som sendes til feilkø.
	 * @throws BrevTechnicalException
	 */
	public void deadLetter(MessageVO msgVO) throws BrevTechnicalException {
		String methodSig = "PEMessageProducer.deadLetter()";

		boolean setReplyQueue = ConfigManager.getInstance().getBool(ConfigManager.DEADLETTER_REPLY_QUEUE_PE, true)
				&& msgVO.getReplyQueueName() != null;

		//sendToDeadLetter(msgVO, p, setReplyQueue, JMSAccessor.getAccessorUsingQueueJndiName(Konstanter.KONF_DEAD_LETTER_PE));
	}

	/**
	 * Sender kvittering til saksbehandlingsystemet som initierte opprettelsen av brevet
	 *
	 * @param peBrevStatusVO
	 * @param messageVo
	 * @param kvittering
	 * @throws BrevTechnicalException
	 */
	public void sendKvittering(BrevStatusVO peBrevStatusVO, MessageVO messageVo, KvitteringVO kvittering)
			throws BrevTechnicalException {
		boolean useJndi = false;

		ArgumentValidator.isNotNull(peBrevStatusVO);
		ArgumentValidator.isNotNull(kvittering);

		String methSig = "PEMessageProducer.sendKvittering(" + peBrevStatusVO.getBrevreferanse() + ")";

		if (peBrevStatusVO.getReturKoe() == null || "".equals(peBrevStatusVO.getReturKoe())) {
			log.debug(methSig, "Returkø mangler, bruker standard: " + Konstanter.KONF_SEND_REPLY_PE);
			peBrevStatusVO.setReturKoe(Konstanter.KONF_SEND_REPLY_PE);
			useJndi = true;
		}

		try {
			// Sende kvitteringsmelding til saksbehandlingsystemet
			XMLService service = XMLServiceFactory.getInstance().createXMLService();
			String xmlKvittering = service.unmarshal(kvittering, peBrevStatusVO);
			//String logMsg = "Sender kvittering, brevStatus: " + getReturstatusString(kvittering, peBrevStatusVO);
		//	log.info(methSig, logMsg);
			sendReturMelding(peBrevStatusVO.getReturKoe(), useJndi, messageVo.getCorrelationID(), xmlKvittering);
		} catch (BrevTechnicalException bte) {
			log.error(methSig, "Fikk ikke sendt kvittering til " + peBrevStatusVO.getReturKoe());
			throw bte;
		}
	}

	/**
	 * Sender statusinformasjon til returkø definert ved bestilling av brev.
	 *
	 * @param queueName     - navn på kø hvor statusmeldingen sendes.
	 * @param useJndi       - om JNDI skal benyttes
	 * @param correlationID - meldingens correlactionID
	 * @param kvittering    - kvitteringsinformasjon
	 * @throws BrevTechnicalException - ved alle feil
	 */
	public void sendReturMelding(String queueName, boolean useJndi, String correlationID, String kvittering)
			throws BrevTechnicalException {
		String methSig = "PEMessageProducer.sendReturMelding";

		if (queueName == null || "".equals(queueName)) {
			log.debug(methSig, "Returkø mangler, bruker standard: " + Konstanter.KONF_SEND_REPLY_PE);
			queueName = Konstanter.KONF_SEND_REPLY_PE;
			useJndi = true;
		}

		produserTextMelding(queueName, null, correlationID, kvittering);
	}
}