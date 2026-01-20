package no.nav.brevserver;

import no.nav.brevserver.core.CoreConfig;
import no.nav.brevserver.core.config.jms.JmsConfig;
import no.nav.brevserver.core.properties.BrevserverProperties;
import no.nav.brevserver.core.properties.FagarkivProperties;
import no.nav.brevserver.core.properties.MqGatewayProperties;
import no.nav.brevserver.core.properties.NaisProperties;
import no.nav.brevserver.joark.SoapConsumerConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.retry.annotation.EnableRetry;

@EnableJms
@EnableRetry
@EnableConfigurationProperties({
		MqGatewayProperties.class,
		BrevserverProperties.class,
		FagarkivProperties.class,
		BrevserverProperties.class,
		NaisProperties.class
})
@Import({
		CoreConfig.class,
		JmsConfig.class,
		SoapConsumerConfiguration.class
})
@Configuration
public class Appconfig {
}
