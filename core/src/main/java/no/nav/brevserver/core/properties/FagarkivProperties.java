package no.nav.brevserver.core.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

	@Valid
	private final Endpoints endpoints = new Endpoints();
	@Valid
	private final Serviceuser serviceuser = new Serviceuser();

	@Data
	@Validated
	public static class Endpoints {
		/**
		 * URL til oppslagstjenesten i fagarkivet.
		 */
		@NotBlank
		private String journal;
		@NotBlank
		private String journalbehandling;

	}

	@Data
	@Validated
	public static class Serviceuser {
		/**
		 * Brukernavn til onprem AD servicebruker.
		 */
		@NotBlank
		@ToString.Exclude
		private String username;
		/**
		 * Passord til onprem AD servicebruker.
		 */
		@NotBlank
		@ToString.Exclude
		private String password;
	}
}
