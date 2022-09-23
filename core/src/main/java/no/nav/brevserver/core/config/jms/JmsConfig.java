package no.nav.brevserver.core.config.jms;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.jms.JmsConstants;
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
import javax.net.ssl.SSLSocketFactory;

import static com.ibm.mq.constants.CMQC.MQENC_NATIVE;
import static com.ibm.msg.client.jms.JmsConstants.JMS_IBM_CHARACTER_SET;
import static com.ibm.msg.client.jms.JmsConstants.JMS_IBM_ENCODING;
import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CM_CLIENT;

@Profile({"nais", "local"})
@Configuration
public class JmsConfig {

	private static final int UTF_8_WITH_PUA = 1208;
	private static final String ANY_TLS13_OR_HIGHER = "*TLS13ORHIGHER";

	@Bean
	public ConnectionFactory wmqConnectionFactory(final MqGatewayProperties mqGatewayAlias,
												  final SrvAppserverProperties srvAppserverProperties) throws JMSException {
		return createConnectionFactory(mqGatewayAlias, srvAppserverProperties);
	}

	private PooledConnectionFactory createConnectionFactory(final MqGatewayProperties mqGatewayAlias,
															final SrvAppserverProperties srvAppserverProperties) throws JMSException {
		MQConnectionFactory connectionFactory = new MQConnectionFactory();
		connectionFactory.setHostName(mqGatewayAlias.getHostname());
		connectionFactory.setPort(mqGatewayAlias.getPort());
		connectionFactory.setQueueManager(mqGatewayAlias.getName());
		connectionFactory.setTransportType(WMQ_CM_CLIENT);
		connectionFactory.setCCSID(UTF_8_WITH_PUA);
		connectionFactory.setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE);
		connectionFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
		connectionFactory.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, true);

		if (mqGatewayAlias.getChannel().isEnabletls()) {
			connectionFactory.setSSLCipherSuite(ANY_TLS13_OR_HIGHER);
			SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			connectionFactory.setSSLSocketFactory(sslSocketFactory);
			connectionFactory.setChannel(mqGatewayAlias.getChannel().getSecurename());
		} else {
			connectionFactory.setChannel(mqGatewayAlias.getChannel().getName());
		}

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
