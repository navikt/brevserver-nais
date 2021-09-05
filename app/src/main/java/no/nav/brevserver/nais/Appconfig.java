package no.nav.brevserver.nais;

import no.nav.brevserver.core.alias.MqGatewayAlias;
import no.nav.brevserver.core.properties.SrvAppserverProperties;
import no.nav.brevserver.service.config.ServiceConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@EnableConfigurationProperties({
		SrvAppserverProperties.class,
		MqGatewayAlias.class
})
@Import({
		NaisContract.class,
		ServiceConfig.class
})
@Configuration
public class Appconfig {

}
