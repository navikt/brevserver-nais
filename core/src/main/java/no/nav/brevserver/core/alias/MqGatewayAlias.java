package no.nav.brevserver.core.alias;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ToString
@ConfigurationProperties("mqgateway01")
@Validated
public class MqGatewayAlias {
	@NotEmpty
	private String hostname;
	@NotEmpty
	private String name;
	@Min(0)
	private int port;
	private boolean tlsbroker;
}
