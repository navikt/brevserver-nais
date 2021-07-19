package no.nav.brevserver.fagarkiv.dokarkiv.impl;

import no.nav.brevserver.dokarkiv.converter.HentDokumentToBrevVoConverter;
import no.nav.brevserver.dokarkiv.impl.NavHeaders;
import no.nav.brevserver.fagarkiv.FagarkivProperties;
import no.nav.brevserver.fagarkiv.dokarkiv.DokarkivConsumer;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevVO;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpMethod.GET;

@Service
public class DokarkivConsumerBean implements DokarkivConsumer {

	//TODO: Correct format?
	public static final String MDC_CALL_ID = "systemId";
	private final RestTemplate restTemplate;
	private final HentDokumentToBrevVoConverter converter;


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
	}

	@Override
	public void lagreDokument(String brevreferanse, String contentType, byte[] brevData) throws BrevTechnicalException {

	}

	@Override
	public void lagreFerdigstiltDokument(String brevreferanse, BrevVO redBrevVO, BrevVO pdfBrevVO) throws BrevTechnicalException {

	}

	@Override
	public boolean isJournalpost(String brevReferanse) throws BrevTechnicalException {
		return false;
	}

	private HttpHeaders createCorrelationIdHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.set(NavHeaders.NAV_CALLID, getCallId());
		return headers;
	}

	public static String getCallId() {
		final String callId = MDC.get(MDC_CALL_ID);
		return isBlank(callId) ? UUID.randomUUID().toString() : callId;
	}
}
