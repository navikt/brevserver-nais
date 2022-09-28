package no.nav.brevserver;

import no.nav.brevserver.nais.Appconfig;
import no.nav.brevserver.nais.DokumentbehandlingResource;
import no.nav.brevserver.service.config.ServiceConfig;
import no.nav.brevserver.ws.WebServiceConfig;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({DokumentbehandlingResource.class, ServiceConfig.class, Appconfig.class, WebServiceConfig.class})
@EnableJwtTokenValidation(ignore = {"org.springframework", "springfox.documentation"})
public class BrevserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrevserverApplication.class, args);
	}
}