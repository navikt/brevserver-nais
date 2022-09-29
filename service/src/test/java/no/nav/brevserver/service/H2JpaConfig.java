package no.nav.brevserver.service;

import no.nav.brevserver.core.CoreConfig;
import no.nav.brevserver.core.alias.BrevserverProperties;
import no.nav.brevserver.core.alias.FagarkivProperties;
import no.nav.brevserver.core.alias.MqGatewayProperties;
import no.nav.brevserver.service.config.ServiceConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableConfigurationProperties({
		FagarkivProperties.class,
		MqGatewayProperties.class,
		BrevserverProperties.class
})
@Import({ServiceConfig.class, CoreConfig.class, JmsItestConfig.class})
@EnableAutoConfiguration
@PropertySource("classpath:application-itest.properties")
@EnableTransactionManagement
@ComponentScan(basePackages = {
		"no.nav.brevserver"
})
public class H2JpaConfig {

}
