package no.nav.brevserver.arkiverBrev.ArkiverBrevDefault;

import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.MessageVO;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.component.jms.JmsBinding;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.Serializable;

@Component
public class MessageVoMapper {

	JmsBinding binding = new JmsBinding();

	//TODO: Tviler på at denne vil funke. Må ryddes en del i. Evt endre måten det gjøres på
	@Handler
	public MessageVO mapMessageVo(Exchange exchange) throws BrevTechnicalException, BrevFunctionalException, JMSException {

		ActiveMQTextMessage message = createActiveMQMessage(exchange);
		binding.appendJmsProperties(message, exchange);
		MessageVO mzg = MessageVO(message);
		return mzg;
	}

	//veldig halvtenkt
	private static ActiveMQMessage createActiveMQMessage(Exchange exchange) throws JMSException, BrevFunctionalException {
		Object body = exchange.getIn().getBody();
		if( body != null)
			ActiveMQTextMessage answer = new ActiveMQTextMessage();
			answer.setText((String) body);
			return answer;
		}
		throw new BrevFunctionalException("MessageBody er tom!");
	}


}
