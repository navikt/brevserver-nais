package no.nav.brevserver.ws;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;

import java.util.Collections;

@Configuration
public class WebServiceConfig {

	@Bean
	ServletRegistrationBean<?> webServicesRegistration(ApplicationContext applicationContext) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(applicationContext);
		servlet.setTransformWsdlLocations(true);
		return new ServletRegistrationBean<>(servlet, "/Dokumentbehandling/*", "/Loggmottak/*");
	}

	@Bean(name = "loggMottak")
	public Wsdl11Definition wsdl11DefinitionLoggmottak() {
		SimpleWsdl11Definition wsdl11Definition = new SimpleWsdl11Definition();
		wsdl11Definition.setWsdl(new ClassPathResource("META-INF/wsdl/Loggmottak.wsdl"));
		return wsdl11Definition;
	}

	@Bean(name = "dokumentbehandling")
	public Wsdl11Definition defaultWsdl11DefinitionDokumentbehandling() {
		SimpleWsdl11Definition wsdl11Definition = new SimpleWsdl11Definition();
		wsdl11Definition.setWsdl(new ClassPathResource("META-INF/wsdl/Dokumentbehandling.wsdl"));
		return wsdl11Definition;
	}

	@Bean
	public MarshallingPayloadMethodProcessor methodProcessor(Jaxb2Marshaller marshaller) {
		return new MarshallingPayloadMethodProcessor(marshaller);
	}

	@Bean
	DefaultMethodEndpointAdapter endpointAdapter(MarshallingPayloadMethodProcessor methodProcessor) {
		DefaultMethodEndpointAdapter adapter = new DefaultMethodEndpointAdapter();
		adapter.setMethodArgumentResolvers(Collections.singletonList(methodProcessor));
		adapter.setMethodReturnValueHandlers(Collections.singletonList(methodProcessor));
		return adapter;
	}

	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setContextPaths(
				"no.nav.tjenester.brevogarkiv.dokumentbehandling",
				"no.nav.tjenester.brevogarkiv.loggmottak"
		);
		marshaller.setCheckForXmlRootElement(false);
		marshaller.setMtomEnabled(true);
		return marshaller;
	}

}