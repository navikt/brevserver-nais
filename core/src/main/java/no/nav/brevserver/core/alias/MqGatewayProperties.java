package no.nav.brevserver.core.alias;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

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
		@NotEmpty
		private String name;
		@NotBlank
		private String securename;
		private boolean enabletls;
	}
}
