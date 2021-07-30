package no.nav.brevserver.arkiverBrev.config;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;


@Configuration
@Profile("itest")
public class JmsItestConfig {

    @Bean
    public Queue mottakArkiv(@Value("${mottak_arkiv.queuename}") String mottakArkivQueueName){
        return new ActiveMQQueue(mottakArkivQueueName);
    }

    @Bean
    public Queue mottakOnline(@Value("${mottak_arkiv_pe.queuename}") String mottakArkivPeQueueName){
        return new ActiveMQQueue(mottakArkivPeQueueName);
    }

    @Bean
    public Queue deadletter(){
        return new ActiveMQQueue("ActiveMQ.DLQ");
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
