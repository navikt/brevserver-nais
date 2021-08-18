package no.nav.brevserver.fagarkiv.dokarkiv.impl;

import no.nav.brevserver.dokarkiv.converter.HentDokumentToBrevVoConverter;
import no.nav.brevserver.dokarkiv.impl.NavHeaders;
import no.nav.brevserver.fagarkiv.FagarkivProperties;
import no.nav.brevserver.fagarkiv.dokarkiv.DokarkivConsumer;
import no.nav.brevserver.fagarkiv.dokarkiv.model.OppdaterJournalpostRequest;
import no.nav.brevserver.fagarkiv.dokarkiv.model.OppdaterJournalpostResponse;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class DokarkivConsumerBean implements DokarkivConsumer {

	//TODO: Correct format?
	public static final String MDC_CALL_ID = "systemId";
	private final RestTemplate restTemplate;
	private final HentDokumentToBrevVoConverter converter;
	private final boolean forsoekFerdigstill = false;
	private String oppdaterJournalpostUrl;

	private static final String OPPDATER_JOURNALPOST_RESOURCE_PATH = "/rest/journalpostapi/v1/journalpost/{journalpostId}";


	@Autowired
	public DokarkivConsumerBean(final RestTemplateBuilder restTemplateBuilder,
								final FagarkivProperties fagarkivProperties,
								final ClientHttpRequestFactory clientHttpRequestFactory,
								final HentDokumentToBrevVoConverter converter) {
		restTemplate = restTemplateBuilder
				.rootUri(fagarkivProperties.getEndpoints().getDokarkiv())
				.basicAuthentication(fagarkivProperties.getServiceuser().getUsername(),
						fagarkivProperties.getServiceuser().getPassword())
				.setReadTimeout(Duration.ofSeconds(60))
				.setConnectTimeout(Duration.ofSeconds(5))
				.requestFactory(() -> clientHttpRequestFactory)
				.build();
		this.converter = converter;
		String baseUrl = fagarkivProperties.getEndpoints().getDokarkiv();
		this.oppdaterJournalpostUrl = baseUrl.endsWith("/")?baseUrl+OPPDATER_JOURNALPOST_RESOURCE_PATH:baseUrl+"/"+OPPDATER_JOURNALPOST_RESOURCE_PATH;
	}



	@Override
	public void oppdaterJournalpost(OppdaterJournalpostRequest oppdaterJournalpostRequest, String brevreferanse) throws BrevTechnicalException {
		HttpHeaders headers = createHeaders();

		ResponseEntity<OppdaterJournalpostResponse> response = restTemplate.exchange(oppdaterJournalpostUrl, HttpMethod.PUT, new HttpEntity<>(oppdaterJournalpostRequest, headers),
				OppdaterJournalpostResponse.class);

		if (response.getStatusCode().equals(HttpStatus.OK)) {
			return;
		}else {
			throw new BrevTechnicalException(String.format("Brev med referanse %s ikke opprettet"));
		}
	}


	@Override
	public boolean isJournalpost(String brevReferanse) throws BrevTechnicalException {
		return false;
	}

	protected HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		return headers;
	}

}
