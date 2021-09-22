package no.nav.brevserver.service;

import no.nav.brevserver.service.config.ServiceConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Import(ServiceConfig.class)
@EnableAutoConfiguration
@PropertySource("classpath:application-itest.properties")
@EnableTransactionManagement
@ComponentScan(basePackages = {
		"no.nav.brevserver"
})
public class H2JpaConfig {

}
