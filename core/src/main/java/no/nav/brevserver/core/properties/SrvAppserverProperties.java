package no.nav.brevserver.core.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;


@Getter
@Setter
@ToString
@ConfigurationProperties("srvappserver")
@Validated
public class SrvAppserverProperties {
	@NotEmpty
	private String username;
	private String password;

	@PostConstruct
	public void postConstruct() {
		if (password == null) {
			password = "";
		}
	}
}
