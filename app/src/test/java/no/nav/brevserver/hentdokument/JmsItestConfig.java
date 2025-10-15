package no.nav.brevserver.hentdokument;

import com.ibm.mq.jakarta.jms.MQQueue;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("itest")
public class JmsItestConfig {

	@Bean
	public Queue brevReplyPe(@Value("${brev_reply_pe.queuename}") String brevReplyPe) throws JMSException {
		return new MQQueue(brevReplyPe);
	}

}
