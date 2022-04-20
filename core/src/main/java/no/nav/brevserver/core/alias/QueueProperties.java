package no.nav.brevserver.core.alias;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ToString
@ConfigurationProperties("brevserverqueue")
@Validated
public class QueueProperties {

	@NonNull
	private boolean autoStartup;

}
