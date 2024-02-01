package config;

import no.nav.brevserver.core.CoreConfig;
import no.nav.brevserver.core.alias.BrevserverProperties;
import no.nav.brevserver.core.alias.FagarkivProperties;
import no.nav.brevserver.core.alias.MqGatewayProperties;
import no.nav.brevserver.joark.JournalbehandlingConfiguration;
import no.nav.brevserver.service.config.ServiceConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Import(CoreConfig.class)
@EnableAutoConfiguration
@Profile("itest")
@EnableTransactionManagement
@ComponentScan(basePackages = {
		"no.nav.brevserver"
})
public class H2JpaConfig {

}
