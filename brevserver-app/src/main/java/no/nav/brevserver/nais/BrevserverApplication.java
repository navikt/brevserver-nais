package no.nav.brevserver.nais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({DokumentbehandlingResource.class})
public class BrevserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrevserverApplication.class, args);
	}
}