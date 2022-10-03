package no.nav.brevserver;

import no.nav.brevserver.nais.Appconfig;
import no.nav.brevserver.nais.DokumentbehandlingResource;
import no.nav.brevserver.ws.WebServiceConfig;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import static java.lang.System.setProperty;

@SpringBootApplication
@Import({DokumentbehandlingResource.class, Appconfig.class, WebServiceConfig.class})
@EnableJwtTokenValidation(ignore = {"org.springframework", "springfox.documentation"})
public class BrevserverApplication {

	public static void main(String[] args) {
		setProperty("javax.net.ssl.keyStorePassword", System.getenv("BREVSERVERCERT_PASSWORD"));
		SpringApplication.run(BrevserverApplication.class, args);
	}
}