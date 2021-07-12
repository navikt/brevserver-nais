package no.nav.brevserver.mdb;

import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.type.QueueType;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.jms.MessageProducerFactory;

import javax.jms.Message;

public abstract class AbstractMdb {

	protected Log log = null;

	public abstract void onMessage(String methSig, Message message, QueueType queueType);

	protected void stopListenerIfApplicable(BrevException e, MessageVO messageVO) {
		String methSig = "handleException(" + getBrevreferanse(messageVO) + ")";
		if (e.isFeilkode(BrevTechnicalException.JNDI_OPPSLAG_FEILET)
				|| e.isFeilkode(BrevTechnicalException.DATABASE_IKKE_TILGJENGELIG)
				|| e.isFeilkode(BrevTechnicalException.MQ_IKKE_TILGJENGELIG)) {
			RuntimeException re = new RuntimeException(e);
			log.error(methSig, "Stopper lytter på grunn av miljøproblemer", re);
			throw re;
		}
	}

	protected void sendToDeadletterIfApplicable(BrevException e, SystemType systemType, MessageVO messageVO) {
		if (messageVO != null) {
			if (e.isFeilkode(BrevTechnicalException.FEIL_I_XML) || e.isFeilkode(BrevTechnicalException.UGYLDIG_JOURNALSTATUS)) {
				sendMessageToDeadletter(systemType, messageVO);
			}
		}
	}

	private void sendMessageToDeadletter(SystemType systemType, MessageVO messageVO) {
		String methSig = "sendMessageToDeadletter(" + getBrevreferanse(messageVO) + ")";
		try {
			log.error(methSig, "Sender meldingen til deadletter-kø");
			MessageProducerFactory.getInstance().createMessageProducer(systemType).deadLetter(messageVO);
		} catch (BrevTechnicalException e) {
			log.error(methSig, "Greide ikkeåsende melding til deadletter-kø", e);
		}
	}

	private String getBrevreferanse(MessageVO msg) {
		if (msg != null && msg.getBrevreferanse() != null) {
			return msg.getBrevreferanse();
		} else {
			return "ukjent brevreferanse";
		}
	}
}