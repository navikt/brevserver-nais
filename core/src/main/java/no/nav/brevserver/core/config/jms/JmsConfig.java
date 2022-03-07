package no.nav.brevserver.core.config.jms;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.jms.JmsConstants;
import com.ibm.msg.client.wmq.WMQConstants;
import no.nav.brevserver.core.alias.MqGatewayProperties;
import no.nav.brevserver.core.properties.SrvAppserverProperties;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;

@Profile({"nais", "local"})
@Configuration
public class JmsConfig {

	private static final int ISO_8859_1 = 819;

	@Bean
	public ConnectionFactory wmqConnectionFactory(final MqGatewayProperties mqGatewayAlias,
												  final @Value("${brevserverchannel.name}") String channelName,
												  final SrvAppserverProperties srvAppserverProperties) throws JMSException {
		return createConnectionFactory(mqGatewayAlias, channelName, srvAppserverProperties);
	}

	private PooledConnectionFactory createConnectionFactory(final MqGatewayProperties mqGatewayAlias,
															final String channelName,
															final SrvAppserverProperties srvAppserverProperties) throws JMSException {
		MQConnectionFactory connectionFactory = new MQConnectionFactory();
		connectionFactory.setHostName(mqGatewayAlias.getHostname());
		connectionFactory.setPort(mqGatewayAlias.getPort());
		connectionFactory.setChannel(channelName);
		connectionFactory.setQueueManager(mqGatewayAlias.getName());
		connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
		connectionFactory.setCCSID(1208);
		connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, 1208);
		//connectionFactory.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, ISO_8859_1);  MQConstants.MQENC_NATIVE
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
	//bisys -> brevserver
	//brevbestilling fra Bisys
	public Queue onlinebrev(@Value("${onlinebrev.queuename}") String brevserverOnlinebrev) throws JMSException {
		return new MQQueue(brevserverOnlinebrev);
	}

	@Bean
	//exstream -> brevserver
	//arkivversjonen pdf til dagens brevlagret/db2
	public Queue mottakArkiv(@Value("${mottak_arkiv.queuename}") String mottakArkivQueueName) throws JMSException {
		return new MQQueue(mottakArkivQueueName);
	}

	@Bean
	//exstream -> brevserver
	// returkø fra exstream til brevserver
	public Queue mottakOnline(@Value("${mottak_online.queuename}") String brevserverMottakOnline) throws JMSException {
		return new MQQueue(brevserverMottakOnline);
	}

	@Bean
	//brevserver->exstream
	//brevbestillingskøen fra brevserver til exstream
	public Queue dialogueOnline(@Value("${dialogue_online.queuename}") String dialogueOnline) throws JMSException {
		return new MQQueue(dialogueOnline);
	}

	@Bean
	// pesys -> brevserver
	// brevbestilling fra pensjon
	public Queue onlinebrevPe(@Value("${onlinebrev_pe.queuename}") String brevserverOnlinebrevPe) throws JMSException {
		return new MQQueue(brevserverOnlinebrevPe);
	}

	@Bean
	// brevserver -> exstream
	// brevbestillingskøen fra brevserver til exstream
	public Queue dialogueOnlinePe(@Value("${dialogue_online_pe.queuename}") String dialogueOnlinePe) throws JMSException {
		return new MQQueue(dialogueOnlinePe);
	}

	@Bean
	// exstream -> brevserver
	// returkø fra exstream til brevserver
	public Queue mottakArkivPe(@Value("${mottak_arkiv_pe.queuename}") String mottakArkivPeQueueName) throws JMSException {
		return new MQQueue(mottakArkivPeQueueName);
	}

	@Bean
	// exstream -> brevserver
	// returkø fra exstream til brevserver
	public Queue mottakOnlinePe(@Value("${mottak_online_pe.queuename}") String brevserverMottakOnlinePe) throws JMSException {
		return new MQQueue(brevserverMottakOnlinePe);
	}

	@Bean
	// brevserver -> pensjon
	// usikker på hvor den brukes. Dette skal vel være definert i in-meldingen
	// med litt flaks kan vi standarisere det til en kø..
	public Queue brevReplyPe(@Value("${brev_reply_pe.queuename}") String brevReplyPe) throws JMSException {
		return new MQQueue(brevReplyPe);
	}

	@Bean
	public Queue deadletter(@Value("${deadletter.queuename}") String deadletter) throws JMSException {
		return new MQQueue(deadletter);
	}

	@Bean
	public Queue deadletterPe(@Value("${deadletter_pe.queuename}") String deadletterPe) throws JMSException {
		return new MQQueue(deadletterPe);
	}


}
