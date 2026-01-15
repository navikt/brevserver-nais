package no.nav.brevserver.consumer;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.exception.BrevserverTechnicalException;
import no.nav.brevserver.core.properties.BrevserverProperties;
import no.nav.brevserver.token.NaisTexasConsumer;
import no.nav.brevserver.token.NaisTexasRequestInterceptor;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.time.Duration;

import static no.nav.brevserver.token.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@Slf4j
@Component
public class DokarkivConsumer {
	public static final String VARIANT_FORMAT_PRODUKSJON = "PRODUKSJON";
	public static final String VARIANT_FORMAT_ARKIV = "ARKIV";
	private final RestClient restClient;
	private final String dokarkivScope;

	public DokarkivConsumer(RestClient.Builder restClientBuilder,
							NaisTexasConsumer naisTexasConsumer,
							BrevserverProperties brevserverProperties) {
		this.restClient = restClientBuilder.baseUrl(brevserverProperties.getEndpoints().getDokarkiv().getUrl())
				.requestFactory(ClientHttpRequestFactoryBuilder.jdk()
						.withCustomizer(jdkClientHttpRequestFactory ->
								jdkClientHttpRequestFactory.setReadTimeout(Duration.ofMinutes(1)))
						.build())
				.requestInterceptor(new NaisTexasRequestInterceptor(naisTexasConsumer))
				.build();
		this.dokarkivScope = brevserverProperties.getEndpoints().getDokarkiv().getScope();
	}

	@Retryable(retryFor = BrevserverTechnicalException.class)
	public SettBrevdataResponse settBrevdata(long journalpostId, byte[] brevdata, String contentType) {
		return restClient.post()
				.uri("/journalpostapi/v1/journalpost/{journalpostId}/settBrevdata/{variantFormat}",
						journalpostId, mapVariantFormat(contentType))
				.accept(APPLICATION_JSON)
				.header(CONTENT_TYPE, mapContentType(contentType))
				.attribute(TARGET_SCOPE, dokarkivScope)
				.body(new ByteArrayInputStream(brevdata))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (request, response) -> {
					throw new BrevserverTechnicalException("Klarte ikke sette brevdata på journalpostId=" + journalpostId);
				})
				.body(new ParameterizedTypeReference<>() {
				});
	}

	private String mapVariantFormat(String contentType) {
		return switch (contentType) {
			case "text/rtf", "application/rtf" -> "application/rtf";
			case APPLICATION_PDF_VALUE -> APPLICATION_PDF_VALUE;
			default -> throw new IllegalArgumentException("contentType=" + contentType + " støttes ikke");
		};
	}

	private static String mapContentType(String contentType) {
		return switch (contentType) {
			case "text/rtf", "application/rtf" -> VARIANT_FORMAT_PRODUKSJON;
			case APPLICATION_PDF_VALUE -> VARIANT_FORMAT_ARKIV;
			default -> throw new IllegalArgumentException("contentType=" + contentType + " støttes ikke");
		};

	}

}
