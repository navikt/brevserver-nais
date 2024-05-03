package no.nav.brevserver.core.alias;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@EnableConfigurationProperties
@ConfigurationProperties("fagarkiv")
@Validated
public class FagarkivProperties {

	private final Endpoints endpoints = new Endpoints();
	private final Serviceuser serviceuser = new Serviceuser();

	@Data
	@Validated
	public static class Endpoints {
		/**
		 * URL til oppslagstjenesten i fagarkivet.
		 */
		@NotEmpty
		private String journal;
		@NotEmpty
		private String journalbehandling;

	}

	@Data
	@Validated
	public static class Serviceuser {
		/**
		 * Brukernavn til onprem AD servicebruker.
		 */
		@NotEmpty
		@ToString.Exclude
		private String username;
		/**
		 * Passord til onprem AD servicebruker.
		 */
		@NotEmpty
		@ToString.Exclude
		private String password;
	}
}
