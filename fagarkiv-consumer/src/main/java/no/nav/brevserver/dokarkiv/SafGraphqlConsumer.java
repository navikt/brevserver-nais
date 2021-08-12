package no.nav.brevserver.dokarkiv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.dokarkiv.constants.Constants;
import no.nav.brevserver.dokarkiv.exception.MarshalGraphqlRequestToJsonTechnicalException;
import no.nav.brevserver.dokarkiv.exception.SafJournalpostIkkeFunnetFunctionalException;
import no.nav.brevserver.dokarkiv.exception.SafJournalpostQueryTechnicalException;
import no.nav.brevserver.dokarkiv.exception.SafJournalpostQueryUnauthorizedException;
import no.nav.brevserver.dokarkiv.exception.ValidationException;
import no.nav.brevserver.dokarkiv.graphql.GraphQLRequest;
import no.nav.brevserver.dokarkiv.impl.NavHeaders;
import no.nav.brevserver.dokarkiv.journalpost.SafJournalpostTo;
import no.nav.brevserver.dokarkiv.journalpost.SafJsonJournalpost;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;


@Component
@Slf4j
public class SafGraphqlConsumer {

	private static final String OIDC_TOKEN_PREFIX = "Bearer";
	private final RestTemplate restTemplate;
	private final String graphQLurl;

	@Autowired
	public SafGraphqlConsumer(RestTemplateBuilder restTemplateBuilder,
							  @Value("${saf.endpoint.url}") String graphQLurl) {
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
		this.graphQLurl = graphQLurl.endsWith("/")?graphQLurl+"graphql":graphQLurl+"/graphql";
	}

	public SafJournalpostTo performQuery(GraphQLRequest graphQLRequest, String authorizationHeader) {

		try {
			HttpHeaders httpHeaders = createAuthHeaderFromToken(authorizationHeader);

			ResponseEntity test = restTemplate.exchange(graphQLurl, HttpMethod.POST, new HttpEntity<>(requestToJson(graphQLRequest), httpHeaders), SafJsonJournalpost.class);
			ResponseEntity<SafJsonJournalpost> responseEntity = test;
			if (responseEntity.getBody() == null || responseEntity.getBody().getData() == null || responseEntity.getBody()
					.getData().getJournalpost() == null) {
				throw new SafJournalpostIkkeFunnetFunctionalException("Ingen journalpost ble funnet");
			}

			return responseEntity.getBody().getJournalpost();

		} catch (HttpClientErrorException e) {
			throw new SafJournalpostQueryUnauthorizedException(String.format("Henting av journalpost feilet med status: %s, feilmelding: %s", e
					.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new SafJournalpostQueryTechnicalException(String.format("Tjenesten SAF (graphQL) feilet med status: %s, feilmelding: %s", e
					.getStatusCode(), e.getMessage()), e);
		}
	}

	private HttpHeaders createAuthHeaderFromToken(String authorizationHeader) {
		HttpHeaders headers = new HttpHeaders();
		if (authorizationHeader == null || !OIDC_TOKEN_PREFIX.equalsIgnoreCase(authorizationHeader.split(" ")[0])) {
			throw new ValidationException("Authorization header må være på formen Bearer {token}");
		}

		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(NavHeaders.NAV_CALLID, MDC.get(Constants.CALL_ID));
		headers.set(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PREFIX + " " + authorizationHeader.split(" ")[1]);
		return headers;
	}

	private String requestToJson(GraphQLRequest graphQLRequest) {
		try {
			return new ObjectMapper().writeValueAsString(graphQLRequest);
		} catch (JsonProcessingException e) {
			throw new MarshalGraphqlRequestToJsonTechnicalException(String.format("Kunne ikke konvertere graphQlRequest til json, feilmelding=%s", e
					.getMessage()), e);
		}
	}
}
