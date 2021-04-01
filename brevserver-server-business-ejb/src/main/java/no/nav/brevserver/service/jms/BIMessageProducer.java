package no.nav.brevserver.service.jms;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.jms.JMSAccessor;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.utility.ArgumentValidator;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.xml.XMLService;
import no.nav.brevserver.service.xml.XMLServiceFactory;

import java.io.ByteArrayOutputStream;

/**
 * Produserer JMS tekstmeldinger, fortrinnsvis i XML.
 */
public class BIMessageProducer extends MessageProducer {

	public BIMessageProducer() {
		log = new Log(this.getClass());
	}

	/**
	 * Sender en bestilling til Dialogue
	 *
	 * @param message meldingen er direkte kopiert fra MessageConsumer
	 * @throws BrevTechnicalException ved alle feil
	 */
	public void sendToDialogue(MessageVO message) throws BrevTechnicalException {
		produserTextMelding(Konstanter.KONF_SEND_DIALOGUE_ONLINE_BI, true, null, message.getCorrelationID(),
				message.getStringBody());
	}

	/**
	 * Sender en melding til deadletter køen for BiSys.
	 *
	 * @param msgVO meldingen som skal sendes
	 * @throws BrevTechnicalException ved alle feil
	 */
	public void deadLetter(MessageVO msgVO) throws BrevTechnicalException {
		String methodSig = "BIMessageProducer.deadLetter()";
		PerformanceLogger p = new PerformanceLogger(methodSig);

		boolean setReplyQueue = ConfigManager.getInstance().getBool(ConfigManager.DEADLETTER_REPLY_QUEUE_BI, true)
				&& msgVO.getReplyQueueName() != null;

		sendToDeadLetter(msgVO, p, setReplyQueue, JMSAccessor.getAccessorUsingQueueJndiName(Konstanter.KONF_DEAD_LETTER_BI));
	}

	/**
	 * Sender kvittering til saksbehandlingsystemet som initierte opprettelsen av brevet
	 *
	 * @param brevStatusVo
	 * @param kvittering
	 * @throws BrevTechnicalException
	 */
	public void sendKvittering(BrevStatusVO brevStatusVo, MessageVO messageVo, KvitteringVO kvittering)
			throws BrevTechnicalException {
		ArgumentValidator.isNotNull(brevStatusVo);
		ArgumentValidator.isNotNull(kvittering);

		String methSig = "ArkiverBrevCommand.sendKvittering(" + brevStatusVo.getBrevreferanse() + ")";

		if (brevStatusVo.getReturKoe() == null || "".equals(brevStatusVo.getReturKoe())) {
			log.warning(methSig, "Kan ikke sende kvittering da returkø mangler");
			return;
		}

		try {
			// Sende kvitteringsmelding til saksbehandlingsystemet
			XMLService service = XMLServiceFactory.getInstance().createXMLService();
			String xmlKvittering = service.unmarshal(kvittering, brevStatusVo);
			log.debug(methSig, "ReturQueue: " + brevStatusVo.getReturKoe());
			String logMsg = "Sender kvittering, brevStatus: " + getReturstatusString(kvittering, brevStatusVo);
			log.info(methSig, logMsg);
			sendReturMelding(brevStatusVo.getReturKoe(), false, messageVo.getCorrelationID(), xmlKvittering);
		} catch (BrevTechnicalException bte) {
			log.error(methSig, "Fikk ikke sendt kvittering til " + brevStatusVo.getReturKoe());
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
		if (queueName == null || "".equals(queueName)) {
			log.debug("MessageProducer.sendReturMelding()", "Forsøkte å sende melding til en kø uten navn");
		} else {
			produserTextMelding(queueName, useJndi, null, correlationID, kvittering);
		}
	}
}