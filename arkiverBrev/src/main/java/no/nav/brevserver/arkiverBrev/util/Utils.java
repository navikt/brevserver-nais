package no.nav.brevserver.arkiverBrev.util;

import com.ibm.jms.JMSMessage;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import org.apache.camel.Exchange;
import org.apache.camel.component.jms.JmsBinding;

import javax.jms.JMSException;
import javax.jms.Message;

public class Utils {

	public static final String REPLY_QUEUE_NAME = "replyQueueName";

	static JmsBinding binding;

	static {
		binding = new JmsBinding();
	}

	public static MessageVO createMessageVoFromExchange(Exchange exchange) throws JMSException, BrevTechnicalException {
		//TODO: blir dette riktig når man senere tar instanceof textMessage / byteMessage?
		//Fra det jeg kan se fra docs skal det gå bra
		//Konverterer til gammel JMS message for lettere håndtering
		Message message = exchange.getIn(Message.class);
		MessageVO messageVO = new MessageVO(exchange.getIn(JMSMessage.class));
		exchange.setProperty(REPLY_QUEUE_NAME, messageVO.getReplyQueueName());
		binding.appendJmsProperties(message, exchange);
		return messageVO;
	}


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
	}

}
