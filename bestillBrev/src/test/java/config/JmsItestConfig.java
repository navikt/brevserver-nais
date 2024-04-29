package config;

import no.nav.brevserver.core.config.jms.JmsConfig;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import jakarta.jms.ConnectionFactory;
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

	@Bean
	public Queue mottakArkivBq(){return new ActiveMQQueue("mottakArkivBq");}

	@Bean
	public Queue mottakArkivPeBq(){return new ActiveMQQueue("mottakArkivPeBq");}

	@Bean
	public Queue bestillBrevBq(){return new ActiveMQQueue("bestillBrevBq");}

	@Bean
	public Queue bestillBrevPeBq(){return new ActiveMQQueue("bestillBrevPeBq");}

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
