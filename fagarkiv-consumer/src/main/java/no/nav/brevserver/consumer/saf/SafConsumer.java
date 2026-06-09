package no.nav.brevserver.consumer.saf;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevFinnesIkkeException;
import no.nav.brevserver.core.exception.BrevTilgangException;
import no.nav.brevserver.core.exception.BrevserverFunctionalException;
import no.nav.brevserver.core.exception.BrevserverTechnicalException;
import no.nav.brevserver.core.properties.BrevserverProperties;
import no.nav.brevserver.token.NaisTexasConsumer;
import no.nav.brevserver.token.NaisTexasRequestInterceptor;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static no.nav.brevserver.token.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@Slf4j
public class SafConsumer {

	private final RestClient restClient;
	private final String safScope;

	public SafConsumer(RestClient.Builder restClientBuilder,
					   NaisTexasConsumer naisTexasConsumer,
					   BrevserverProperties brevserverProperties) {
		this.restClient = restClientBuilder.baseUrl(brevserverProperties.getEndpoints().getSaf().getUrl())
				.requestFactory(ClientHttpRequestFactoryBuilder.jdk()
						.withCustomizer(jdkClientHttpRequestFactory ->
								jdkClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(20)))
						.build())
				.requestInterceptor(new NaisTexasRequestInterceptor(naisTexasConsumer))
				.build();
		this.safScope = brevserverProperties.getEndpoints().getSaf().getScope();
	}

	@Retryable(BrevserverTechnicalException.class)
	public SafJournalpost performQuery(GraphQLRequest graphQLRequest) {
		GraphQLResponse graphQLResponse = restClient.post()
				.uri("/graphql")
				.accept(APPLICATION_JSON)
				.attribute(TARGET_SCOPE, safScope)
				.body(graphQLRequest)
				.retrieve()
				.onStatus(HttpStatusCode::is5xxServerError, (_, response) -> {
					throw new BrevserverTechnicalException("Klarte ikke hente journalpost metadata. Status " + response.getStatusCode());
				})
				.requiredBody(GraphQLResponse.class);
		return graphQLResponse.getJournalpost();
	}

	@Retryable(BrevserverTechnicalException.class)
	public SafDokument hentDokument(String journalpostId, String dokumentInfoId, String variantFormat) {
		return restClient.get()
				.uri("/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}", journalpostId, dokumentInfoId, variantFormat)
				.attribute(TARGET_SCOPE, safScope)
				.exchange((_, clientResponse) -> {
					if (clientResponse.getStatusCode().is4xxClientError()) {
						if (clientResponse.getStatusCode() == NOT_FOUND) {
							throw new BrevFinnesIkkeException(journalpostId);
						} else if (clientResponse.getStatusCode() == FORBIDDEN) {
							ProblemDetail problemDetail = clientResponse.bodyTo(ProblemDetail.class);
							if (problemDetail == null) {
								throw new BrevTilgangException(journalpostId, "Avvist");
							}
							String message = (String) problemDetail.getProperties().getOrDefault("message", "Avvist");
							throw new BrevTilgangException(journalpostId, message);
						} else {
							throw new BrevserverFunctionalException("Klarte ikke hente brev med journalpostId %s, dokumentInfoId %s, variantFormat %s"
									.formatted(journalpostId, dokumentInfoId, variantFormat));
						}
					} else if (clientResponse.getStatusCode().is5xxServerError()) {
						throw new BrevserverTechnicalException("Klarte ikke hente brev med journalpostId %s, dokumentInfoId %s, variantFormat %s"
								.formatted(journalpostId, dokumentInfoId, variantFormat));
					}
					return new SafDokument(clientResponse.bodyTo(byte[].class), clientResponse.getHeaders().getContentType());
				});
	}
}
