package no.nav.brevserver.nais;

import no.nav.brevserver.service.config.ServiceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({DokumentbehandlingResource.class, ServiceConfig.class, Appconfig.class})
public class BrevserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrevserverApplication.class, args);
	}
}