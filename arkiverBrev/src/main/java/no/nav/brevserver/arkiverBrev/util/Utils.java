package no.nav.brevserver.arkiverBrev.util;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.MessageVO;
import org.apache.camel.Exchange;
import org.apache.camel.component.jms.JmsBinding;
import org.apache.camel.component.jms.JmsMessage;

import javax.jms.Message;

public class Utils {

	public static final String URI = "uri";

	static JmsBinding binding;

	static {
		binding = new JmsBinding();
	}

	public static MessageVO getMessageVoFromExchange(Exchange exchange) throws BrevTechnicalException {
		Message message = exchange.getIn(JmsMessage.class).getJmsMessage();
		MessageVO messageVO = new MessageVO(message);
		return messageVO;
	}

}
