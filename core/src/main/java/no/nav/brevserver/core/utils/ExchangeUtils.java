package no.nav.brevserver.core.utils;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.vo.MessageVO;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;

import javax.jms.JMSException;
import javax.jms.Queue;

@Slf4j
public class ExchangeUtils {

	public enum SendToMode {
		OPPRETT_BREV,
		GI_TILBAKEMELDING,
		GI_FEILMELDING,
		INGEN_TILBAKEMELDING,
		TIL_FEILKO
	}

	public static final String DESTINATION = "CamelJmsDestinationName";
	public static final String JMS = "jms:";
	public static final String SENDTOMODE = "SENDTOMODE";
	public static final String PROPERTY_SENDTOMODE = "property.SENDTOMODE";
	private static final String IBM_HEADER_REPLYTO = "JMSReplyTo";
	public static final String DEFAULT_RETURN_QUEUE = "defaultReturnQueue";

	public static final String OVERRIDE_DESTINATION = "CamelJmsDestinationName";
	//dummy blir overskrevet av OVERRIDE_DESTINATION automatisk i camel.
	//Brukes fordi vanlige camel endpoints ikke gotar ibm options, mens denne gjør det
	public static final String JMS_OVERRIDDEN = "jms:dummy";

	public static MessageVO getMessageVoFromExchange(Exchange exchange) {

		MessageVO vo = new MessageVO(exchange.getIn().getBody(byte[].class));
		vo.setStringBody(exchange.getIn().getBody(String.class));

		try {
			vo.setReplyQueueName(getReplyTo(exchange));
			log.info("Setter replyQ til: " + vo.getReplyQueueName());
		} catch (JMSException e) {
			log.error("Klarte ikke hente replyq");
		}


		return vo;
	}

	private static String getReplyTo(Exchange exchange) throws JMSException {
		if (exchange.getIn() != null && exchange.getIn().getHeaders() != null
				&& exchange.getIn().getHeaders().get(IBM_HEADER_REPLYTO) != null) {

			return ((Queue) exchange.getIn().getHeaders().get(IBM_HEADER_REPLYTO)).getQueueName();
		} else {
			log.warn("Ingen returKø er definert. Setter returkø til default for routen.");
			return (String) exchange.getIn().getHeaders().get(DEFAULT_RETURN_QUEUE);
		}
	}

	public static void setBodyAndMode(Exchange exchange, Object Body, SendToMode sendToMode) {
		exchange.getIn().setBody(Body);
		exchange.setProperty(SENDTOMODE, sendToMode);
	}

	/*
	 * Brukes for å sende svar tilbake til meldinger som kommer som parameter i jms-messagen
	 * Camel er ikke veldig glad i ibm-headers virker det som.
	 */
	public static void setBodyAndReturnQueueOverriddenWithMode(Exchange exchange, Object Body, String returnQueue, SendToMode sendToMode) {
		exchange.getIn().setBody(Body);
		String returKo = StringUtils.isBlank(returnQueue) ?	(String) exchange.getIn().getHeader(DEFAULT_RETURN_QUEUE) : returnQueue;
		exchange.getIn().setHeader(OVERRIDE_DESTINATION, returKo);
		exchange.setProperty(SENDTOMODE, sendToMode.name());
		log.info("Setter returkø til: " + returKo + " og sendToMode til: " + sendToMode);
	}

	/*
	 * Metode for å override destination.
	 * z/os krever ?targetClient=1 optionen som er mq spesifikk og ikke blir godtatt av camel
	 * Ved å sende denne propertien håndterer ibm-mq selv hvor meldingen skal sendes og overstyrer camel sin to()
	 */
	public static void overrideDestination(Exchange exchange, String newDestination){
		exchange.getIn().setHeader(OVERRIDE_DESTINATION, newDestination+"?targetClient=1");
	}

	public static void setDefaultReturnQueue(Exchange exchange, String returnQueue){
		exchange.getIn().setHeader(DEFAULT_RETURN_QUEUE, returnQueue);
	}


	public static String createCamelFriendlyIbmReturnQueue(String oldQ) {
		return StringUtils.split(oldQ, "?")[0];
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
}
