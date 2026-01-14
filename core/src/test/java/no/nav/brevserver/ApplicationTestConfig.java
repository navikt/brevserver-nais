package no.nav.brevserver;

import no.nav.brevserver.core.CoreConfig;
import no.nav.brevserver.core.properties.BrevserverProperties;
import no.nav.brevserver.core.properties.FagarkivProperties;
import no.nav.brevserver.core.properties.MqGatewayProperties;
import no.nav.brevserver.core.properties.NaisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("itest")
@EnableConfigurationProperties({
		MqGatewayProperties.class,
		BrevserverProperties.class,
		FagarkivProperties.class,
		NaisProperties.class
})
@Import({CoreConfig.class, JmsItestConfig.class})
@ComponentScan(basePackages = "no.nav.brevserver")
public class ApplicationTestConfig {
}
