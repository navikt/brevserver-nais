package config;

import com.ibm.mq.jms.MQQueue;
import no.nav.brevserver.core.config.jms.JmsConfig;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;


@Configuration
@Profile("itest")
@Import({JmsConfig.class})
public class JmsItestConfig {

	@Bean
	public Queue onlinebrev(@Value("${onlinebrev.queuename}") String brevserverOnlinebrev) {
		return new ActiveMQQueue(brevserverOnlinebrev);
	}

	@Bean
	public Queue dialogueOnline(@Value("${dialogue_online.queuename}") String brevserverOnlinebrev) {
		return new ActiveMQQueue(brevserverOnlinebrev);
	}

	@Bean
	// pesys -> brevserver
	// brevbestilling fra pensjon
	public Queue onlinebrevPe(@Value("${onlinebrev_pe.queuename}") String brevserverOnlinebrevPe) throws JMSException {
		return new ActiveMQQueue(brevserverOnlinebrevPe);
	}

	@Bean
	// brevserver -> exstream
	// brevbestillingskøen fra brevserver til exstream
	public Queue dialogueOnlinePe(@Value("${dialogue_online_pe.queuename}") String dialogueOnlinePe) throws JMSException {
		return new ActiveMQQueue(dialogueOnlinePe);
	}

	@Bean
	public Queue brevReplyPe(@Value("${brev_reply_pe.queuename}") String brevReplyPe) throws JMSException {
		return new MQQueue(brevReplyPe);
	}

	@Bean
	public Queue deadletter() {
		return new ActiveMQQueue("DLQ");
	}

	@Bean
	public Queue deadletterPe() {
		return new ActiveMQQueue("DLQ");
	}

	@Bean
	public Queue svarKo() {
		return new ActiveMQQueue("SvarKo");
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public BrokerService broker() {
		BrokerService service = new BrokerService();
		service.setPersistent(false);
		return service;
	}

	@Bean
	public ConnectionFactory activemqConnectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost?create=false");
		RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
		redeliveryPolicy.setMaximumRedeliveries(0);
		activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
		return activeMQConnectionFactory;
	}
}
