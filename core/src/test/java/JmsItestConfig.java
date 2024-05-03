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
	public Queue deadletter() {
		return new ActiveMQQueue("mottakDLQ");
	}

	@Bean
	public Queue svarKo() {
		return new ActiveMQQueue("mottakSvarKo");
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
