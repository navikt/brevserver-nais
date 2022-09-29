package no.nav.brevserver.nais;

import no.nav.brevserver.core.CoreConfig;
import no.nav.brevserver.core.alias.BrevserverProperties;
import no.nav.brevserver.core.alias.FagarkivProperties;
import no.nav.brevserver.core.alias.MqGatewayProperties;
import no.nav.brevserver.core.config.jms.JmsConfig;
import no.nav.brevserver.joark.JournalbehandlingConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@EnableConfigurationProperties({
		MqGatewayProperties.class,
		BrevserverProperties.class,
		FagarkivProperties.class
})
@Import({
		NaisContract.class,
		CoreConfig.class,
		JmsConfig.class,
		JournalbehandlingConfiguration.class
})
@Configuration
public class Appconfig {

}
