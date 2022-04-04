package no.nav.brevserver.service;

import com.ibm.mq.jms.MQQueue;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;


@Configuration
@Profile("itest")
public class JmsItestConfig {

	@Bean
	public Queue brevReplyPe(@Value("${brev_reply_pe.queuename}") String brevReplyPe) throws JMSException {
		return new MQQueue(brevReplyPe);
	}

}
