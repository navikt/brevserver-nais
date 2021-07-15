package no.nav.brevserver.mdb;

import javax.jms.JMSException;
import javax.jms.Message;

import no.nav.brevserver.command.AbstractCommand;
import no.nav.brevserver.command.CommandFactory;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.QueueType;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.MessageVO;

/**
 * Klasse foråbehandle innkommende meldinger av type Bidrag
 * 
 * @author Marius Thåring, Visma Consulting
 * 
 */
public abstract class BiAbstractMdb extends AbstractMdb {

	public void onMessage(String methSig, Message message, QueueType queueType) {
		PerformanceLogger p = new PerformanceLogger(methSig);
		MessageVO messageVO = null;
		try {
			messageVO = new MessageVO(message);
			AbstractCommand cmd = CommandFactory.getInstance().createCommand(queueType, messageVO);
			cmd.execute();
		} catch (BrevException e) {
			sendToDeadletterIfApplicable(e, SystemType.BI, messageVO);
			stopListenerIfApplicable(e, messageVO);
		}
		try {
			message.acknowledge();
		} catch (JMSException e) {
			stopListenerIfApplicable(new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG,
					"Greide ikkeåta melding av kø", e), messageVO);
		} finally {
			p.stop();
		}
	}
}