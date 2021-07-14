package no.nav.brevserver.core.config.jms;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.jms.JmsConstants;
import com.ibm.msg.client.wmq.WMQConstants;
import no.nav.brevserver.core.alias.MqGatewayAlias;
import no.nav.brevserver.core.properties.SrvAppserverProperties;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import javax.jms.JMSException;
import javax.jms.Queue;

@Profile({"nais", "local"})
@Configuration
public class JmsConfig {

	private static final int UTF_8_WITH_PUA = 1208;

	private PooledConnectionFactory createConnectionFactory(final MqGatewayAlias mqGatewayAlias,
															final String channelName,
															final SrvAppserverProperties srvAppserverProperties) throws JMSException {
		MQConnectionFactory connectionFactory = new MQConnectionFactory();
		connectionFactory.setHostName(mqGatewayAlias.getHostname());
		connectionFactory.setPort(mqGatewayAlias.getPort());
		connectionFactory.setChannel(channelName);
		connectionFactory.setQueueManager(mqGatewayAlias.getName());
		connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
		connectionFactory.setCCSID(UTF_8_WITH_PUA);
		connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQConstants.MQENC_NATIVE);
		connectionFactory.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
		UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
		adapter.setTargetConnectionFactory(connectionFactory);

		PooledConnectionFactory pooledFactory = new PooledConnectionFactory();
		pooledFactory.setConnectionFactory(adapter);
		pooledFactory.setMaxConnections(10);
		pooledFactory.setMaximumActiveSessionPerConnection(10);

		connectionFactory.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, false);
		adapter.setUsername(srvAppserverProperties.getUsername());
		adapter.setPassword(srvAppserverProperties.getPassword());

		return pooledFactory;
	}

	@Bean
	public Queue mottakArkiv(@Value("${mottak_arkiv_queuename}") String mottakArkivQueueName) throws JMSException {
		return new MQQueue(mottakArkivQueueName);
	}

	@Bean
	public Queue mottakArkivPe(@Value("${mottak_arkiv_pe_queuename}") String mottakArkivPeQueueName) throws JMSException {
		return new MQQueue(mottakArkivPeQueueName);
	}

	@Bean
	public Queue dialogueOnline(@Value("${dialogue_online_queuename}") String dialogueOnline) throws JMSException {
		return new MQQueue(dialogueOnline);
	}

	@Bean
	public Queue dialogueOnlinePe(@Value("${dialogue_online_pe_queuename}") String dialogueOnlinePe) throws JMSException {
		return new MQQueue(dialogueOnlinePe);
	}

	@Bean
	public Queue onlinebrev(@Value("${onlinebrev_queuename}") String brevserverOnlinebrev) throws JMSException {
		return new MQQueue(brevserverOnlinebrev);
	}

	@Bean
	public Queue onlinebrevPe(@Value("${onlinebrev_pe_queuename}") String brevserverOnlinebrevPe) throws JMSException {
		return new MQQueue(brevserverOnlinebrevPe);
	}

	@Bean
	public Queue mottakOnline(@Value("${mottak_online_queuename}") String brevserverMottakOnline) throws JMSException {
		return new MQQueue(brevserverMottakOnline);
	}

	@Bean
	public Queue mottakOnlinePe(@Value("${mottak_online_pe_queuename}") String brevserverMottakOnlinePe) throws JMSException {
		return new MQQueue(brevserverMottakOnlinePe);
	}

	@Bean
	public Queue brevReplyPe(@Value("${brev_reply_pe_queuename}") String brevReplyPe) throws JMSException {
		return new MQQueue(brevReplyPe);
	}

	@Bean
	public Queue deadletter(@Value("${deadletter_queuename}") String deadletter) throws JMSException {
		return new MQQueue(deadletter);
	}

	@Bean
	public Queue deadletterPe(@Value("${deadletter_pe_queuename}") String deadletterPe) throws JMSException {
		return new MQQueue(deadletterPe);
	}


}
