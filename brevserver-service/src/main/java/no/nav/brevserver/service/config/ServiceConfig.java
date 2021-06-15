package no.nav.brevserver.service.config;

import no.nav.brevserver.repository.RepositoryConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = {
		"no.nav.brevserver.service"
})
@Import(RepositoryConfig.class)
public class ServiceConfig {


}
