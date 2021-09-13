package no.nav.brevserver.core.alias;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@ConfigurationProperties("mqgateway01")
@Validated
public class MqGatewayProperties {
	@NotBlank
	private String hostname;
	@NotBlank
	private String name;
	@Min(0)
	private int port;
	private boolean tlsbroker;
}
