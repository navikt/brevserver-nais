package no.nav.brevserver.hentdokument;

import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiremock.spring.EnableWireMock;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;

@EnableMockOAuth2Server
@EnableWireMock
public abstract class AbstractOauth2Test {

	private static final String AZUREV2_ISSUER = "azurev2";
	protected static final String OID = "4e5a62ad-a76d-4a67-8eac-8ff15b3b48fa";

	@Autowired
	public MockOAuth2Server mockOAuth2Server;

	public String jwt() {
		return jwt(ofEntries(entry("oid", OID)));
	}

	public String jwt(String scp) {
		return jwt(ofEntries(entry("oid", OID), entry("scp", scp)));
	}

	public String jwtMachinToMachine(String roles) {
		return jwt(ofEntries(entry("oid", OID), entry("idtyp", "app"), entry("roles", roles)));
	}

	private String jwt(Map<String, String> claims) {
		String audience = "brevserver-nais";
		return mockOAuth2Server.issueToken(
				AZUREV2_ISSUER,
				"app-clientid",
				new DefaultOAuth2TokenCallback(
						AZUREV2_ISSUER,
						"subject",
						"JWT",
						List.of(audience),
						claims,
						60
				)
		).serialize();
	}

}
