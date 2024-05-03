package config;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("itest")
public class JmsItestConfig {

	@Bean
	public Queue mottakArkiv(@Value("${mottak_arkiv.queuename}") String mottakArkivQueueName) {
		return new ActiveMQQueue(mottakArkivQueueName);
	}

	@Bean
	public Queue mottakOnlineLinux(@Value("${mottak_online_linux.queuename}") String mottakOnlineQueueName) {
		return new ActiveMQQueue(mottakOnlineQueueName);
	}

	@Bean
	// exstream -> brevserver
	// returkø fra exstream til brevserver
	public Queue mottakArkivPeLinux(@Value("${mottak_arkiv_pe_linux.queuename}") String mottakArkivPeQueueName) {
		return new ActiveMQQueue(mottakArkivPeQueueName);
	}

	@Bean
	// exstream -> brevserver
	// returkø fra exstream til brevserver
	public Queue mottakOnlinePeLinux(@Value("${mottak_online_pe_linux.queuename}") String brevserverMottakOnlinePe) {
		return new ActiveMQQueue(brevserverMottakOnlinePe);
	}

	@Bean
	public Queue brevReplyPe(@Value("${brev_reply_pe.queuename}") String brevReplyPe) {
		return new ActiveMQQueue(brevReplyPe);
	}

	@Bean
	public Queue mottakSvarKo() {
		return new ActiveMQQueue("mottakSvarKo");
	}

	@Bean
	public Queue deadletter() {
		return new ActiveMQQueue("mottakDLQ");
	}

	@Bean
	public Queue deadletterPe() {
		return new ActiveMQQueue("mottakDLQ");
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public EmbeddedActiveMQ broker() {
		EmbeddedActiveMQ service = new EmbeddedActiveMQ();
		service.setConfigResourcePath("artemis-server.xml");
		return service;
	}

	@Bean
	public ConnectionFactory activemqConnectionFactory(EmbeddedActiveMQ embeddedActiveMQ) { // EmbeddedActiveMQ must be initialized before we try to connect, therefore we depend on it here
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost?create=false");

		JmsPoolConnectionFactory pooledFactory = new JmsPoolConnectionFactory();
		pooledFactory.setConnectionFactory(activeMQConnectionFactory);
		pooledFactory.setMaxConnections(1);
		return pooledFactory;
	}
}
