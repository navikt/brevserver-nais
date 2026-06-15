package no.nav.brevserver;

import no.nav.brevserver.ws.WebServiceConfig;
import no.nav.brevserver.ws.dokumentbehandling.DokumentbehandlingResource;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({DokumentbehandlingResource.class, Appconfig.class, WebServiceConfig.class})
@EnableJwtTokenValidation
public class BrevserverApplication {

	static void main(String[] args) {
		SpringApplication.run(BrevserverApplication.class, args);
	}
}