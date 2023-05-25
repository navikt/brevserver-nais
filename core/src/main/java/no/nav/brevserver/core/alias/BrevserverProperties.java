package no.nav.brevserver.core.alias;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Positive;


@Data
@ConfigurationProperties("brevserverproperties")
@Validated
public class BrevserverProperties {

	private boolean loggXML;
	private final Database database = new Database();

	@Data
	@Validated
	public static class Database {
		/**
		 * Statisk pool verdi for dokarkiv databasen.
		 * <p>
		 * Optimizing UCP behaviour https://docs.oracle.com/database/121/JJUCP/optimize.htm#JJUCP8143
		 * About Optimizing Real-World Performance with Static Connection Pools
		 * https://docs.oracle.com/en/database/oracle/oracle-database/19/jjucp/optimizing-real-world-performance.html
		 * select STAT_NAME, to_char(VALUE) as VALUE, COMMENTS from v$osstat where stat_name IN ('NUM_CPUS','NUM_CPU_CORES','NUM_CPU_SOCKETS');
		 * Ledige connections i brevserver-databasen er 750, så lenge vi holder oss under dette skal ting gå bra.
		 * Vi deler disse ressursene med andre, men etter samtale med dba skal det ikke være noe problem å bruke inntil ~360 om vi trenger dette.
		 * 60 koblinger per pod er godt nok for dokarkiv så det burde holde i massevis for brevserver.
		 *
		 * @see no.nav.brevserver.core.repository.RepositoryConfig
		 */
		@Positive
		private int poolsize = 60;
	}

}
