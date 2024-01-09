package no.nav.brevserver.service;

import com.ibm.mq.jakarta.jms.MQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.jms.JMSException;
import jakarta.jms.Queue;


@Configuration
@Profile("itest")
public class JmsItestConfig {

	@Bean
	public Queue brevReplyPe(@Value("${brev_reply_pe.queuename}") String brevReplyPe) throws JMSException {
		return new MQQueue(brevReplyPe);
	}

}
