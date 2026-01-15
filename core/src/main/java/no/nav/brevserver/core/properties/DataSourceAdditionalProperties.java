package no.nav.brevserver.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("database")
public record DataSourceAdditionalProperties(String onshosts) {
}
