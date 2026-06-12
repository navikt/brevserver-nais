package no.nav.brevserver.core.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;


@Data
@ConfigurationProperties("brevserver")
@Validated
public class BrevserverProperties {

	private boolean loggXml;
	@Valid
	private final Serviceuser serviceuser = new Serviceuser();
	@Valid
	private final Database database = new Database();
	@Valid
	private final Endpoints endpoints = new Endpoints();

	@Data
	public static class Database {
		/// Statisk pool verdi for dokarkiv databasen.
		///
		/// [Optimizing UCP behaviour ](https://docs.oracle.com/database/121/JJUCP/optimize.htm#JJUCP8143)
		///
		/// [About Optimizing Real-World Performance with Static Connection Pools](https://docs.oracle.com/en/database/oracle/oracle-database/19/jjucp/optimizing-real-world-performance.html)
		///
		/// ```
        /// select STAT_NAME, to_char(VALUE) as VALUE, COMMENTS from v$osstat where stat_name IN ('NUM_CPUS','NUM_CPU_CORES','NUM_CPU_SOCKETS');
        /// ```
		/// Ledige connections i brevserver-databasen er 750, så lenge vi holder oss under dette skal ting gå bra.
		/// Vi deler disse ressursene med andre, men etter samtale med dba skal det ikke være noe problem å bruke inntil ~360 om vi trenger dette.
		/// 60 koblinger per pod er godt nok for dokarkiv så det burde holde i massevis for brevserver.
		///
		/// @see no.nav.brevserver.core.repository.RepositoryConfig
		@Positive
		private int poolsize = 60;
	}

	@Data
	public static class Endpoints {
		@NotNull
		@Valid
		private EntraEndpoint dokarkiv;
		@NotNull
		@Valid
		private EntraEndpoint saf;
	}

	@Data
	public static class EntraEndpoint {
		/// Url til tjeneste som har entra autorisasjon
		@NotNull
		private URI url;

		/// Scope til entra client credential flow
		@NotEmpty
		private String scope;
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
