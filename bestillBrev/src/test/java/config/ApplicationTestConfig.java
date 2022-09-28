package config;

import no.nav.brevserver.core.alias.BrevserverProperties;
import no.nav.brevserver.core.alias.MqGatewayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("itest")
@EnableConfigurationProperties({
		MqGatewayProperties.class,
		BrevserverProperties.class
})
@Import({JmsItestConfig.class})
@ComponentScan(basePackages = "no.nav.brevserver")
public class ApplicationTestConfig {
}
