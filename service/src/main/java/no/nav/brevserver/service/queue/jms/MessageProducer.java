package no.nav.brevserver.service.queue.jms;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.ByteArrayOutputStream;

import static no.nav.brevserver.service.queue.KvitteringRoute.DIRECT_SENDKVITTERINGROUTE;

//TODO: Bytt med route/*

/**
 * Beskrivelse av klassen
 * 
 * @author Dag Kristiansen
 */
@Slf4j
public abstract class MessageProducer {

	public static final String URI = "uri";

	@Autowired
	private ProducerTemplate producerTemplate;

	public abstract void sendToDialogue(MessageVO message) throws BrevTechnicalException;

	public abstract void deadLetter(MessageVO msgVO) throws BrevTechnicalException;

	public abstract void sendKvittering(BrevStatusVO brevStatusVo, MessageVO messageVo, KvitteringVO kvittering)
			throws BrevTechnicalException;

	public abstract void sendReturMelding(String queueName, boolean useJndi, String correlationID, String kvittering)
			throws BrevTechnicalException;

	/**
	 * Sender en JMS ByteMelding.
	 *
	 * @param bytearray
	 * @param sendQueueName
	 * @param useJndi
	 * @throws BrevTechnicalException

	public final void sendByteMelding(byte[] bytearray, String sendQueueName, boolean useJndi) throws BrevTechnicalException {
	String methodSig = "MessageProducer.sendByteMelding(" + sendQueueName + ")";

	PerformanceLogger p = new PerformanceLogger(methodSig);

	JMSAccessor jmsAccessor = null;

	try {
	if (useJndi) {
	jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(sendQueueName);
	} else {
	jmsAccessor = JMSAccessor.getAccessorUsingQueueName(sendQueueName);
	}

	BytesMessage bytesMessage = jmsAccessor.createBytesMessage();
	bytesMessage.writeBytes(bytearray);

	jmsAccessor.sendMessage(bytesMessage, 0);
	} catch (JMSException jms) {
	throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, jms);

	} finally {
	JMSAccessor.close(jmsAccessor);
	p.stop();
	}
	}*/

	/**
	 * Denne metoden sender et redigert rtf-dokument til brevpakken
	 *
	 * @param bos               Strøm med xml-header + *.rtf)
	 * @param sendQueueJndiName - Info om til hvilken flytservice det skal rutes til (finnes i xml-konfigfil). Vær obs på at dersom
	 *                          nesteFlytService er noe annet enn til brevpakken (en feil har oppstått), så må meldingen konverteres til en
	 *                          tekstmelding og sende status til saksbehandlingssystemet.
	 * @param useJndi           Om JNDI skal benyttes ved oppslag av kø
	 * @throws BrevTechnicalException
	 */
	public final void sendRTF(ByteArrayOutputStream bos, String sendQueueJndiName, boolean useJndi)
			throws BrevTechnicalException {
		//sendByteMelding(bos.toByteArray(), sendQueueJndiName, useJndi);
	}

	/**
	 * Sender en JMS Text melding
	 *
	 * @param queueName     Køen meldingen skal sendes på
	 * @param textMessage   Melding vi ønsker å legge på køen. Hvis null, så benyttes teksten i meldings-argumentet
	 * @param correlationID - meldingens korrelasjonsID
	 * @param melding       - melding vi ønsker å sende. TextMessage må være null for at denne skal benyttes.
	 * @throws BrevTechnicalException - ved alle feil
	 */

	protected void produserTextMelding(String queueName, TextMessage textMessage, String correlationID, String melding) throws BrevTechnicalException {
		try {
			if (textMessage == null) {
				log.debug("Text message was null, creating message");
				textMessage = new ActiveMQTextMessage();
				textMessage.setJMSCorrelationID(correlationID);
				textMessage.setText(melding);
			}

			int size = textMessage.getText() == null ? 0 : textMessage.getText().length();
			log.debug("Sender textmelding på " + queueName + " Size:" + size);
			producerTemplate.sendBodyAndHeader(DIRECT_SENDKVITTERINGROUTE,
					textMessage,
					URI, queueName);
		} catch (JMSException jms) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, jms);
		}
	}

	/**
	 * Sends a message to the dead-letter queue
	 *
	 * @param msgVO
	 * @param p
	 * @param setReplyQueue
	 * @param jmsAccessor
	 * @throws BrevTechnicalException

	protected final void sendToDeadLetter(MessageVO msgVO, PerformanceLogger p, boolean setReplyQueue, JMSAccessor jmsAccessor)
	throws BrevTechnicalException {
	try {
	if (msgVO.hasByteBody()) {
	BytesMessage bytesMessage = jmsAccessor.createBytesMessage();
	bytesMessage.writeBytes(msgVO.getByteBody());
	if (setReplyQueue) {
	JMSAccessor jmsAccessorReplyQueue = JMSAccessor.getAccessorUsingQueueName(msgVO.getReplyQueueName());
	bytesMessage.setJMSReplyTo(jmsAccessorReplyQueue != null ? jmsAccessorReplyQueue.getQueue() : null);
	}
	bytesMessage.setJMSCorrelationID(msgVO.getCorrelationID());
	jmsAccessor.sendMessage(bytesMessage, 0);
	} else {
	TextMessage textMessage = jmsAccessor.createTextMessage("");
	textMessage.setJMSCorrelationID(msgVO.getCorrelationID());
	textMessage.setText(msgVO.getStringBody());
	if (setReplyQueue) {
	JMSAccessor jmsAccessorReplyQueue = JMSAccessor.getAccessorUsingQueueName(msgVO.getReplyQueueName());
	textMessage.setJMSReplyTo(jmsAccessorReplyQueue != null ? jmsAccessorReplyQueue.getQueue() : null);
	}
	jmsAccessor.sendMessage(textMessage, 0);
	}
	} catch (JMSException jms) {
	throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, jms);

	} finally {
	JMSAccessor.close(jmsAccessor);
	p.stop();
	}
	}*/

	/**
	 * Builds status logg message for kvittering
	 * @param kvittering
	 * @param brevStatus
	 * @return Status message for logging

	public static String getReturstatusString(KvitteringVO kvittering, BrevStatusVO brevStatus) {
	StringBuffer sb = new StringBuffer();
	if (brevStatus.getStatus() != null) {
	sb.append(" brevstatus: ").append(brevStatus.getStatus());
	}
	if (kvittering.getLagerStatus() !=  null) {
	sb.append(" lagerstatus: ").append(kvittering.getLagerStatus());
	}
	if (kvittering.getFeilkode() != null) {
	sb.append(" feilkode: ").append(kvittering.getFeilkode());
	}
	if (kvittering.getFeilniva() != null ) {
	sb.append(" feilnivå: ").append(kvittering.getFeilniva());
	}
	return sb.toString();
	}*/
}
