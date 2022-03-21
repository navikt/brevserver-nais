package no.nav.brevserver.core.utils;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.vo.MessageVO;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.regex.Pattern;

import static no.nav.brevserver.core.mdc.MDCConstants.MDC_CALL_ID;

@Slf4j
public class ExchangeUtils {

	public enum SendToMode {
		OPPRETT_BREV,
		GI_TILBAKEMELDING,
		GI_FEILMELDING,
		INGEN_TILBAKEMELDING
	}

	public static final String DESTINATION = "CamelJmsDestinationName";
	public static final String JMS = "jms:";
	public static final String SENDTOMODE = "SENDTOMODE";
	public static final String PROPERTY_SENDTOMODE = "property.SENDTOMODE";
	private static final String JMSReplyTo = "JMSReplyTo";
	public static final String DEFAULT_RETURN_QUEUE = "defaultReturnQueue";

	public static final String OVERRIDE_DESTINATION = "CamelJmsDestinationName";
	//dummy blir overskrevet av OVERRIDE_DESTINATION automatisk i camel.
	public static final String JMS_OVERRIDDEN = "jms:dummy";


	private static Pattern containsQueuemanager = Pattern.compile("//(.*)/");

	public static MessageVO getMessageVoFromExchange(Exchange exchange) {

		MessageVO vo = new MessageVO(exchange.getIn().getBody(byte[].class));
		vo.setStringBody(exchange.getIn().getBody(String.class));
		MDC.put(MDC_CALL_ID, exchange.getExchangeId());

		try {
			vo.setReplyQueueName(getReplyTo(exchange));
			log.info("Setter messageVo.replyQ til: " + vo.getReplyQueueName());
		} catch (JMSException e) {
			log.error("Klarte ikke hente replyq");
		}


		return vo;
	}

	/*
	 * Brukes for å sende svar tilbake til meldinger som kommer som parameter i jms-messagen
	 * Camel er ikke veldig glad i ibm-headers virker det som.
	 */
	public static void setBodyAndReturnQueueWithMode(Exchange exchange, Object Body, String returnQueue, SendToMode sendToMode) {
		exchange.getIn().setBody(Body);
		String returKo = StringUtils.isBlank(returnQueue) ? (String) exchange.getIn().getHeader(DEFAULT_RETURN_QUEUE) : returnQueue;
		setDestination(exchange, stripQueueManager(returKo));
		exchange.setProperty(SENDTOMODE, sendToMode.name());
		log.info("Setter returkø til: " + returKo + " og sendToMode til: " + sendToMode);
	}

	/*
	 * Meldingene definerer selv hva som er returnQueue. Mange av disse inneholder ?targetclient?=1 som crasher camel.
	 * Setter derfor returnqueue i den ibm-spesifikke OVERRIDE_DESTINATION
	 */
	public static void setDestination(Exchange exchange, String newDestination) {
		exchange.getIn().setHeader(OVERRIDE_DESTINATION, setTargetClientForQueue(newDestination));
		log.info("Setter returkø til: " + exchange.getIn().getHeader(OVERRIDE_DESTINATION));
	}

	public static void setBodyAndMode(Exchange exchange, Object Body, SendToMode sendToMode) {
		exchange.getIn().setBody(Body);
		exchange.setProperty(SENDTOMODE, sendToMode);
	}

	public static void setDefaultReturnQueue(Exchange exchange, String returnQueue) {
		exchange.getIn().setHeader(DEFAULT_RETURN_QUEUE, setTargetClientForQueue(returnQueue));
	}

	public static void notEmpty(String name, String value, boolean checkIfValidNumber) throws BrevException {
		if (value == null) {
			return;
		} else if (value.equals("")) {
			throw new BrevException(name + " er blank (ikke null)");
		} else if (checkIfValidNumber) {
			try {
				Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new BrevException(name + " er ikke et gyldig tall", e);
			}
		}
	}

	private static String getReplyTo(Exchange exchange) throws JMSException {
		if (exchange.getIn() != null && exchange.getIn().getHeaders() != null
				&& exchange.getIn().getHeaders().get(JMSReplyTo) != null) {

			return ((Queue) exchange.getIn().getHeaders().get(JMSReplyTo)).getQueueName();
		} else {
			return (String) exchange.getIn().getHeaders().get(DEFAULT_RETURN_QUEUE);
		}
	}

	/*
	 * Vi lar mq selv bestemme hvilken queuemanager køen tilhører ved å fjerne den spesifikke.
	 */
	private static String stripQueueManager(String queuename){
		return containsQueuemanager.matcher(queuename).replaceAll("///");
	}

	/*
	 * Metode for å override destination.
	 * z/os krever ?targetClient=1 optionen som er mq spesifikk og ikke blir godtatt av camel
	 * Ved å sende denne propertien håndterer ibm-mq selv hvor meldingen skal sendes og overstyrer camel sin to()
	 */
	private static String setTargetClientForQueue(String queuename) {
		return queuename.toLowerCase().contains("targetclient") ? queuename : queuename + "?targetClient=1";
	}
}
