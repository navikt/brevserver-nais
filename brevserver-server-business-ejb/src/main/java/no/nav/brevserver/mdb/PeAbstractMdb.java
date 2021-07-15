package no.nav.brevserver.mdb;

import javax.jms.JMSException;
import javax.jms.Message;

import no.nav.brevserver.command.AbstractCommand;
import no.nav.brevserver.command.CommandFactory;
import no.nav.brevserver.command.PEArkiverBrevCommand;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.QueueType;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.jms.MessageProducer;
import no.nav.brevserver.service.jms.MessageProducerFactory;
import no.nav.brevserver.service.xml.XMLService;
import no.nav.brevserver.service.xml.XMLServiceFactory;

/**
 * Klasse foråbehandle innkommende meldinger av type Pensjon
 * 
 * @author Marius Thåring, Visma Consulting
 * 
 */
public abstract class PeAbstractMdb extends AbstractMdb {

	public void onMessage(String methSig, Message message, QueueType queueType) {
		PerformanceLogger p = new PerformanceLogger(methSig);
		MessageVO messageVO = null;
		AbstractCommand command = null;
		try {
			messageVO = new MessageVO(message);
			command = CommandFactory.getInstance().createCommand(queueType, messageVO);
			command.execute();
		} catch (BrevException e) {
			try {
				sendFeilmelding(command, e);
			} catch (BrevTechnicalException e1) {
				stopListenerIfApplicable(e1, messageVO);
			}
			sendToDeadletterIfApplicable(e, SystemType.PE, messageVO);
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

	private void sendFeilmelding(AbstractCommand command, BrevException e) throws BrevTechnicalException {
		if (command != null && !(command instanceof PEArkiverBrevCommand)) {
			BrevStatusVO status = (BrevStatusVO) command.getResult();
			if (status != null && status.getReturKoe() != null) {
				String feilmelding;
				if (e.isFeilkode(BrevTechnicalException.FEIL_I_XML)) {
					feilmelding = Konstanter.FEIL_MELDING_UGYLDIG;
				} else {
					feilmelding = Konstanter.FEIL_UKJENT;
				}
				KvitteringVO kvittering = new KvitteringVO();
				kvittering.setBrevreferanse(status.getBrevreferanse());
				kvittering.setSystemID(status.getSystemID());
				kvittering.setFeilkode(feilmelding);
				status.setStatus(Konstanter.BREVSTATUS_FEIL);

				XMLService service = XMLServiceFactory.getInstance().createXMLService();
				String feilMelding = service.unmarshal(kvittering, status);

				MessageProducer producer = MessageProducerFactory.getInstance().createMessageProducer(SystemType.PE);
				producer.sendReturMelding(status.getReturKoe(), false, null, feilMelding);
			}
		}
	}
}