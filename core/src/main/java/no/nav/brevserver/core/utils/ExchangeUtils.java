package no.nav.brevserver.core.utils;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.vo.MessageVO;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.Map;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
public class ExchangeUtils {

	public enum SendToMode {
		OPPRETT_BREV,
		GI_TILBAKEMELDING,
		GI_FEILMELDING,
		INGEN_TILBAKEMELDING
	}

	public static final String JMS = "jms:";
	public static final String SENDTOMODE = "SENDTOMODE";
	public static final String JMSReplyTo = "JMSReplyTo";
	public static final String DEFAULT_RETURN_QUEUE = "defaultReturnQueue";

	public static final String OVERRIDE_DESTINATION = "CamelJmsDestinationName";
	//dummy blir overskrevet av OVERRIDE_DESTINATION automatisk i camel.
	public static final String JMS_OVERRIDDEN = "jms:dummy";


	private static Pattern containsQueuemanager = Pattern.compile("//(.*)/");

	private static Pattern containsReadAheadAllowed = Pattern.compile("(&readAheadAllowed=1)|(readAheadAllowed=1&)|(\\?readAheadAllowed=1)(?!&)");
	private static Pattern containsPutAsync = Pattern.compile("(&putAsyncAllowed=1)|(putAsyncAllowed=1&)|(\\?putAsyncAllowed=1)(?!&)");
	public static MessageVO getMessageVoFromExchange(Exchange exchange) {

		MessageVO vo = new MessageVO(exchange.getIn().getBody(byte[].class));
		vo.setStringBody(exchange.getIn().getBody(String.class));

		if(log.isDebugEnabled()) {
			log.debug("JMS-headers: " + getJMSHeaders(exchange));
		}


		try {
			vo.setReplyQueueName(getReplyTo(exchange));
		} catch (JMSException e) {
			log.error("Klarte ikke hente replyq");
		}

		return vo;
	}

	public static String getJMSHeaders(Exchange exchange){
		String JMSHeaders = "";
		Map<String, Object> headers = exchange.getIn().getHeaders();

		JMSHeaders += "JMSMessage: " + headers.get("JMSMessage")+"\n";
		JMSHeaders += "JMSType: " + headers.get("JMSType")+"\n";
		JMSHeaders += "JMSDeliveryMode: " + headers.get("JMSDeliveryMode")+"\n";
		JMSHeaders += "JMSDeliveryDelay: " + headers.get("JMSDeliveryDelay")+"\n";
		JMSHeaders += "JMSDeliveryTime: " + headers.get("JMSDeliveryTime")+"\n";
		JMSHeaders += "JMSExpiration: " + headers.get("JMSExpiration")+"\n";
		JMSHeaders += "JMSPriority: " + headers.get("JMSPriority")+"\n";
		JMSHeaders += "JMSMessageID: " + headers.get("JMSMessageID")+"\n";
		JMSHeaders += "JMSTimestamp: " + headers.get("JMSTimestamp")+"\n";
		JMSHeaders += "JMSCorrelationID: " + headers.get("JMSCorrelationID")+"\n";
		JMSHeaders += "JMSDestination: "+ headers.get("JMSDestination")+"\n";
		JMSHeaders += "JMSReplyTo: " + headers.get("JMSReplyTo")+"\n";
		JMSHeaders += "JMSRedelivered: " + headers.get("JMSRedelivered")+"\n";
		JMSHeaders += "JMS_IBM_Format:" + headers.get("JMS_IBM_Format")+"\n";
		JMSHeaders += "JMS_IBM_Character_Set: " + headers.get("JMS_IBM_Character_Set")+"\n";
		JMSHeaders += "JMS_IBM_Encoding: " + headers.get("JMS_IBM_Encoding")+"\n";

		return JMSHeaders;
	}

	/*
	 * Brukes for å sende svar tilbake til meldinger som kommer som parameter i jms-messagen
	 * Camel er ikke veldig glad i ibm-headers virker det som.
	 */
	public static void setBodyAndReturnQueueWithMode(Exchange exchange, Object Body, String returnQueue, SendToMode sendToMode) {
		exchange.getIn().setBody(Body);
		if(returnQueue!=null) {
			returnQueue = returnQueue.trim();
		}
		String returKo = StringUtils.isBlank(returnQueue) ? (String) exchange.getIn().getHeader(DEFAULT_RETURN_QUEUE) : returnQueue;
		String newReturKo = buildReturnQueue(returKo);
		setDestination(exchange, newReturKo);
		exchange.setProperty(SENDTOMODE, sendToMode.name());
	}

	/*
	 * Meldingene definerer selv hva som er returnQueue. Mange av disse inneholder ?targetclient?=1 som crasher camel.
	 * Setter derfor returnqueue i den ibm-spesifikke OVERRIDE_DESTINATION
	 */
	public static void setDestination(Exchange exchange, String newDestination) {
		exchange.getIn().setHeader(OVERRIDE_DESTINATION, setTargetClientForQueue(newDestination));
	}
	public static void setDestinationWithQueueString(Exchange exchange, String newDestination) {
		exchange.getIn().setHeader(OVERRIDE_DESTINATION, buildReturnQueue(newDestination));
	}

	public static void setBodyAndMode(Exchange exchange, Object Body, SendToMode sendToMode) {
		exchange.getIn().setBody(Body);
		exchange.setProperty(SENDTOMODE, sendToMode);
	}

	public static void setMode(Exchange exchange, SendToMode sendToMode) {
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
			return null;
		}
	}


	public static String buildReturnQueue(String queuename){
		String oldQname = queuename;
		if(queuename!=null) {
			queuename = queuename.trim();
		}
		if(!isEmpty(queuename)) {
			//delete queuemanager om den finnes
			queuename = stripQueueManager(queuename);
			//delete unwanted parameters
			queuename = stripExtraParameters(queuename);
			//Set target client om den ikke er satt
			queuename = setTargetClientForQueue(queuename);
			//set queue-string
			queuename = setQueueString(queuename);
		}
		log.debug("original returkø: " + oldQname + " ny returkø: " + queuename);
		return queuename;
	}
	/*
	 * Vi lar mq selv bestemme hvilken queuemanager køen tilhører ved å fjerne den spesifikke.
	 */
	private static String stripQueueManager(String queuename){
		return containsQueuemanager.matcher(queuename).replaceAll("///");
	}

	//Noen parametre gjør at vi ikke klarer å sende til køen. Fjern disse.
	private static String stripExtraParameters(String queuename){
		queuename = containsReadAheadAllowed.matcher(queuename).replaceAll("");
		return containsPutAsync.matcher(queuename).replaceAll("");

	}

	/*
	 * Metode for å override destination.
	 * z/os krever ?targetClient=1 optionen som er mq spesifikk og ikke blir godtatt av camel
	 * Ved å sende denne propertien håndterer ibm-mq selv hvor meldingen skal sendes og overstyrer camel sin to()
	 */
	private static String setTargetClientForQueue(String queuename) {
		if(!isEmpty(queuename)) {
            //targetclient er allerede satt, returner kønavnet som det er
            if(queuename.toLowerCase().contains("targetclient")){
                return queuename;
            }
			//Noen svarkøer inneholder allerede parametre definert etter ?
			//Legg på ?targetclient=1 om det ikke allerede finnes parametre og &targerclient=1 om det finnes parametre fra før
			return queuename.contains("?") ? queuename + "&targetClient=1" : queuename + "?targetClient=1";
		}
        return queuename;
	}

	private static String setQueueString(String queuename){
		if(!isEmpty(queuename) && !queuename.toLowerCase().contains("queue:///")) {
			return "queue:///" + queuename;
		}
		return queuename;
	}
}
