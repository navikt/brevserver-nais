package no.nav.brevserver.nais;

import no.nav.brevserver.core.config.jms.JmsConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;

@Configuration
@ComponentScan
@EnableJms
@Import(JmsConfig.class)
public class JmsConsumerConfig {
}
