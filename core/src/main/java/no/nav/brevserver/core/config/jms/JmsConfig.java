package no.nav.brevserver.core.config.jms;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.jakarta.jms.MQQueue;
import com.ibm.msg.client.jakarta.jms.JmsConstants;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import no.nav.brevserver.core.alias.FagarkivProperties;
import no.nav.brevserver.core.alias.MqGatewayProperties;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import javax.net.ssl.SSLSocketFactory;

import static com.ibm.msg.client.jakarta.jms.JmsConstants.JMS_IBM_CHARACTER_SET;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CM_CLIENT;

@Profile({"nais", "local"})
@Configuration
public class JmsConfig {

	private static final int UTF_8_WITH_PUA = 1208;
	private static final String ANY_TLS13_OR_HIGHER = "*TLS13ORHIGHER";

	@Bean
	public ConnectionFactory wmqConnectionFactory(final MqGatewayProperties mqGatewayAlias,
												  final FagarkivProperties fagarkivProperties) throws JMSException {
		return createConnectionFactory(mqGatewayAlias, fagarkivProperties);
	}

	private JmsPoolConnectionFactory createConnectionFactory(final MqGatewayProperties mqGatewayAlias,
															final FagarkivProperties fagarkivProperties) throws JMSException {
		MQConnectionFactory connectionFactory = new MQConnectionFactory();
		connectionFactory.setHostName(mqGatewayAlias.getHostname());
		connectionFactory.setPort(mqGatewayAlias.getPort());
		connectionFactory.setQueueManager(mqGatewayAlias.getName());
		connectionFactory.setTransportType(WMQ_CM_CLIENT);
		connectionFactory.setCCSID(UTF_8_WITH_PUA);
		connectionFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
		connectionFactory.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, true);

		connectionFactory.setSSLCipherSuite(ANY_TLS13_OR_HIGHER);
		SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		connectionFactory.setSSLSocketFactory(sslSocketFactory);
		connectionFactory.setChannel(mqGatewayAlias.getChannel().getSecurename());

		UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
		adapter.setTargetConnectionFactory(connectionFactory);
		adapter.setUsername(fagarkivProperties.getServiceuser().getUsername());
		adapter.setPassword(fagarkivProperties.getServiceuser().getPassword());

		JmsPoolConnectionFactory pooledFactory = new JmsPoolConnectionFactory();
		pooledFactory.setConnectionFactory(adapter);
		pooledFactory.setMaxConnections(10);
		pooledFactory.setMaxSessionsPerConnection(10);

		return pooledFactory;
	}


	@Bean
	//bisys -> brevserver
	//brevbestilling fra Bisys
	public Queue onlinebrev(@Value("${onlinebrev.queuename}") String brevserverOnlinebrev) throws JMSException {
		return new MQQueue(brevserverOnlinebrev);
	}

	@Bean
	// default inputkø for brevserver
	// ferdigproduserte brev fra system y kommer inn her
	public Queue mottakArkiv(@Value("${mottak_arkiv.queuename}") String mottakArkivQueueName) throws JMSException {
		return new MQQueue(mottakArkivQueueName);
	}

	@Bean
	//exstream -> brevserver
	// returkø fra exstream til brevserver
	public Queue mottakOnlineLinux(@Value("${mottak_online_linux.queuename}") String brevserverMottakOnlineLinux) throws JMSException {
		return new MQQueue(brevserverMottakOnlineLinux);
	}

	@Bean
	//brevserver->exstream
	//brevbestillingskøen fra brevserver til exstream
	public Queue dialogueOnline(@Value("${dialogue_online.queuename}") String dialogueOnline) throws JMSException {
		return new MQQueue(dialogueOnline);
	}

	@Bean
	// pensjon -> brevserver
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
	// default inputkø for brevserver
	// ferdigproduserte brev fra system y kommer inn her
	public Queue mottakArkivPeLinux(@Value("${mottak_arkiv_pe_linux.queuename}") String mottakArkivPeQueueName) throws JMSException {
		return new MQQueue(mottakArkivPeQueueName);
	}

	@Bean
	// exstream -> brevserver
	// returkø fra exstream til brevserver
	public Queue mottakOnlinePeLinux(@Value("${mottak_online_pe_linux.queuename}") String brevserverMottakOnlinePe) throws JMSException {
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
