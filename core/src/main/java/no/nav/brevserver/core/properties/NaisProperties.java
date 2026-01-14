package no.nav.brevserver.core.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("nais")
public record NaisProperties(@NotBlank String appName, @NotBlank String tokenEndpoint) {
}
