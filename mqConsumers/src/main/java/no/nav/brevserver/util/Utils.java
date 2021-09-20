package no.nav.brevserver.util;

import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.MessageVO;
import org.apache.camel.Exchange;
import org.apache.camel.component.jms.JmsMessage;

import javax.jms.Message;

public class Utils {

	public static final String URI = "uri";

	public static MessageVO getMessageVoFromExchange(Exchange exchange) throws BrevTechnicalException {
		Message message = exchange.getIn(JmsMessage.class).getJmsMessage();
		MessageVO messageVO = new MessageVO(message);
		return messageVO;
	}

	public static void setBodyAndReturnQueue(Exchange exchange, Object Body, String returnQueue){
		exchange.getIn().setBody(Body);
		exchange.getIn().setHeader(URI, returnQueue);
	}

	public static void notEmpty_old(String name, String value, boolean checkIfValidNumber) throws BrevException {
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
