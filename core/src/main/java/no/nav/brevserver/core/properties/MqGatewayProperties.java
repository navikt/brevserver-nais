package no.nav.brevserver.core.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@EnableConfigurationProperties
@ConfigurationProperties("mqgateway01")
@Validated
public class MqGatewayProperties {

	@NotBlank
	private String hostname;

	@NotBlank
	private String name;

	@Min(0)
	private int port;

	private MqChannel channel = new MqChannel();

	@Data
	@Validated
	public static class MqChannel {
		@NotBlank
		private String securename;
	}
}
