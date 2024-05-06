package config;

import no.nav.brevserver.core.alias.BrevserverProperties;
import no.nav.brevserver.core.alias.FagarkivProperties;
import no.nav.brevserver.core.alias.MqGatewayProperties;
import no.nav.brevserver.joark.JoarkService;
import no.nav.brevserver.joark.JoarkServiceImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties({
		MqGatewayProperties.class,
		BrevserverProperties.class,
		FagarkivProperties.class
})
@Import({JmsItestConfig.class})
@ComponentScan(basePackages = "no.nav.brevserver", excludeFilters={
		@ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value = JoarkServiceImpl.class)})
public class ApplicationTestConfig {
	@MockBean
	public JoarkService joarkServiceMock;
}
