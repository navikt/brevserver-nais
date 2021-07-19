package no.nav.brevserver;

import no.nav.brevserver.fagarkiv.FagarkivProperties;
import no.nav.brevserver.nais.Appconfig;
import no.nav.brevserver.nais.DokumentbehandlingResource;
import no.nav.brevserver.service.config.ServiceConfig;
import no.nav.brevserver.ws.loggmottak.WebServiceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({DokumentbehandlingResource.class, ServiceConfig.class, Appconfig.class, FagarkivProperties.class, WebServiceConfig.class})
public class BrevserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrevserverApplication.class, args);
	}
}