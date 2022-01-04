package no.nav.brevserver.core.utils.mqUtils;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.MessageVO;
import org.apache.camel.Exchange;

@Slf4j
public class Utils {

	public static final String RETURNQUEUE = "uri";

	public static MessageVO getMessageVoFromExchange(Exchange exchange) throws BrevTechnicalException {

		MessageVO vo = new MessageVO(exchange.getIn().getBody(byte[].class));
		vo.setStringBody(exchange.getIn().getBody(String.class));
		//MessageVO messageVO = new MessageVO(vo, "replyq");
		return vo;
	}

	public static void setBodyAndReturnQueue(Exchange exchange, Object Body, String returnQueue){
		exchange.getIn().setBody(Body);
		exchange.getIn().setHeader(RETURNQUEUE, returnQueue);
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
