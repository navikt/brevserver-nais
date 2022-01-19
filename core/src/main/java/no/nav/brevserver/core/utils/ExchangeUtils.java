package no.nav.brevserver.core.utils;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.MessageVO;
import org.apache.camel.Exchange;

@Slf4j
public class ExchangeUtils {

	public enum SendToMode {
		OPPRETT_BREV,
		GI_TILBAKEMELDING,
		GI_FEILMELDING,
		INGEN_TILBAKEMELDING,
		TIL_FEILKO
	}

	public static final String DESTINATION = "uri";
	public static final String JMS = "jms:";
	public static final String SENDTOMODE = "SENDTOMODE";
	public static final String PROPERTY_SENDTOMODE = "property.SENDTOMODE";

	public static MessageVO getMessageVoFromExchange(Exchange exchange) throws BrevTechnicalException {

		MessageVO vo = new MessageVO(exchange.getIn().getBody(byte[].class));
		vo.setStringBody(exchange.getIn().getBody(String.class));
		log.info("xml:\n" +vo.getStringBody());
		return vo;
	}

	public static void setBodyAndReturnQueue(Exchange exchange, Object Body, String returnQueue){
		exchange.getIn().setBody(Body);
		exchange.getIn().setHeader(DESTINATION, returnQueue);
	}

	public static void setBodyAndReturnQueueWithMode(Exchange exchange, Object Body, String returnQueue, SendToMode sendToMode){
		exchange.getIn().setBody(Body);
		exchange.getIn().setHeader(DESTINATION, returnQueue);
		exchange.setProperty(SENDTOMODE, sendToMode.name());
		log.info("Setter returkø til: " + returnQueue + " og sendToMode til: " + sendToMode);
	}

	public static void setBodyAndMode(Exchange exchange, Object Body, SendToMode sendToMode){
		exchange.getIn().setBody(Body);
		exchange.setProperty(SENDTOMODE, sendToMode);
	}

	public static void setModeAndReturnQueue(Exchange exchange, SendToMode sendToMode, String returnQueue){
		log.info("Gammel mode: " + exchange.getProperty(SENDTOMODE) + " gammel kø: " + exchange.getIn().getHeaders().get(DESTINATION) +
				"Ny mode: " + sendToMode + " ny returnqueue: " + returnQueue);
		exchange.setProperty(SENDTOMODE, sendToMode);
		exchange.getIn().setHeader(DESTINATION, returnQueue);
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
