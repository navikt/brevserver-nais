package no.nav.brevserver;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@Profile("itest")
public class JmsItestConfig {

	@Bean
	public Queue mottakArkiv(@Value("${mottak_arkiv.queuename}") String mottakArkivQueueName) {
		return new ActiveMQQueue(mottakArkivQueueName);
	}

	@Bean
	public Queue deadletter() {
		return new ActiveMQQueue("DLQBi");
	}

	@Bean
	public Queue mottakSvarKo() {
		return new ActiveMQQueue("mottakSvarKo");
	}

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
	public Queue onlinebrevPe(@Value("${onlinebrev_pe.queuename}") String brevserverOnlinebrevPe) {
		return new ActiveMQQueue(brevserverOnlinebrevPe);
	}

	@Bean
	// brevserver -> exstream
	// brevbestillingskøen fra brevserver til exstream
	public Queue dialogueOnlinePe(@Value("${dialogue_online_pe.queuename}") String dialogueOnlinePe) {
		return new ActiveMQQueue(dialogueOnlinePe);
	}

	@Bean
	public Queue brevReplyPe(@Value("${brev_reply_pe.queuename}") String brevReplyPe) {
		return new ActiveMQQueue(brevReplyPe);
	}

	@Bean
	public Queue deadletterPe() {
		return new ActiveMQQueue("DLQPe");
	}

	@Bean
	public Queue mottakArkivBq() {
		return new ActiveMQQueue("mottakArkivBq");
	}

	@Bean
	public Queue mottakArkivPeBq() {
		return new ActiveMQQueue("mottakArkivPeBq");
	}

	@Bean
	public Queue bestillBrevBq() {
		return new ActiveMQQueue("bestillBrevBq");
	}

	@Bean
	public Queue bestillBrevPeBq() {
		return new ActiveMQQueue("bestillBrevPeBq");
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
	public Queue mottakArkivPeLinuxBq() {
		return new ActiveMQQueue("mottakArkivPeLinuxBq");
	}

	@Bean
	// exstream -> brevserver
	// returkø fra exstream til brevserver
	public Queue mottakOnlinePeLinux(@Value("${mottak_online_pe_linux.queuename}") String brevserverMottakOnlinePe) {
		return new ActiveMQQueue(brevserverMottakOnlinePe);
	}


	@Bean(initMethod = "start", destroyMethod = "stop")
	public EmbeddedActiveMQ embeddedActiveMQ() {
		EmbeddedActiveMQ service = new EmbeddedActiveMQ();
		service.setConfigResourcePath("artemis-server.xml");
		return service;
	}

	@Bean
	@DependsOn("embeddedActiveMQ")
	public ConnectionFactory activemqConnectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost?create=false");

		JmsPoolConnectionFactory pooledFactory = new JmsPoolConnectionFactory();
		pooledFactory.setConnectionFactory(activeMQConnectionFactory);
		pooledFactory.setMaxConnections(1);
		return pooledFactory;
	}

	@Bean
	@DependsOn("activemqConnectionFactory")
	public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
		return new JmsTemplate(connectionFactory);
	}
}
