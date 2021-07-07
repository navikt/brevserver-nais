package no.nav.brevserver.nais;

import no.nav.brevserver.core.alias.MqGatewayAlias;
import no.nav.brevserver.core.alias.ServiceuserAlias;
import no.nav.brevserver.core.config.jms.JmsConfig;
import no.nav.brevserver.core.properties.SrvAppserverProperties;
import no.nav.brevserver.service.config.ServiceConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@EnableConfigurationProperties({
		SrvAppserverProperties.class,
		MqGatewayAlias.class,
		ServiceuserAlias.class
})
@Import({
		NaisContract.class,
		ServiceConfig.class,
		JmsConfig.class
})
@Configuration
public class Appconfig {

}
