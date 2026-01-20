package no.nav.brevserver.joark;

import no.nav.brevserver.core.properties.FagarkivProperties;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;

@Configuration
public class SoapConsumerConfiguration {

	private final FagarkivProperties fagarkivProperties;

	public SoapConsumerConfiguration(FagarkivProperties fagarkivProperties) {
		this.fagarkivProperties = fagarkivProperties;
	}

	@Bean
	public Jaxb2Marshaller journalMarshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setContextPath("no.nav.virksomhet.tjenester.arkiv.journal.v2");
		return marshaller;
	}

	@Bean
	public JournalClient journalClient(Jaxb2Marshaller journalMarshaller) {
		JournalClient client = new JournalClient();
		client.setDefaultUri(fagarkivProperties.getEndpoints().getJournal());
		client.setMarshaller(journalMarshaller);
		client.setUnmarshaller(journalMarshaller);
		client.setInterceptors(new ClientInterceptor[]{securityInterceptor()});
		return client;
	}

	public Wss4jSecurityInterceptor securityInterceptor() {
		Wss4jSecurityInterceptor security = new Wss4jSecurityInterceptor();
		security.setSecurementActions(WSConstants.USERNAME_TOKEN_LN);
		security.setSecurementUsername(this.fagarkivProperties.getServiceuser().getUsername());
		security.setSecurementPasswordType(WSConstants.PW_TEXT);
		security.setSecurementPassword(this.fagarkivProperties.getServiceuser().getPassword());
		return security;
	}

}
