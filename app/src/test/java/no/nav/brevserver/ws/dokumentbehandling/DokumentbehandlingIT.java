package no.nav.brevserver.ws.dokumentbehandling;

import no.nav.brevserver.ApplicationTestConfig;
import no.nav.brevserver.hentdokument.AbstractOauth2Test;
import no.nav.brevserver.ws.WebServiceConfig;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_XML;

@SpringBootTest(classes = {ApplicationTestConfig.class, DokumentbehandlingResource.class, WebServiceConfig.class},
		webEnvironment = RANDOM_PORT)
@AutoConfigureTestDatabase
@AutoConfigureRestTestClient
@ActiveProfiles("itest")
public class DokumentbehandlingIT extends AbstractOauth2Test {

	private static final byte[] DOKUMENT = "Hei".getBytes();

	@Autowired
	private RestTestClient restTestClient;

	@Test
	void skalHenteProduksjonDokumentFraJoarkNaarPensjonDokumentOgJournalpostUnderArbeid() throws IOException {
		stubNaisTexas();
		stubSafJournalpostQuery("journalpost-under-arbeid.json");
		stubSafHentDokument("PRODUKSJON", "application/rtf");

		EntityExchangeResult<String> response = restTestClient.post()
				.uri("/Dokumentbehandling")
				.contentType(TEXT_XML)
				.body(fromClassPath("dokumentbehandling-hentdokument-request.xml"))
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(String.class).returnResult();
		assertThat(response.getResponseBody()).contains("hentDokumentResponse");
	}

	@Test
	void skalHenteArkivDokumentFraJoarkNaarPensjonDokumentOgJournalpostFerdigstilt() throws IOException {
		stubNaisTexas();
		stubSafJournalpostQuery("journalpost-ferdigstilt.json");
		stubSafHentDokument("ARKIV", "application/pdf");

		EntityExchangeResult<String> response = restTestClient.post()
				.uri("/Dokumentbehandling")
				.contentType(TEXT_XML)
				.body(fromClassPath("dokumentbehandling-hentdokument-request.xml"))
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(String.class).returnResult();
		assertThat(response.getResponseBody()).contains("hentDokumentResponse");
	}

	@Test
	void skalReturnereFaultNaarDokumentIkkeFinnes() throws IOException {
		stubNaisTexas();
		stubSafJournalpostQuery("journalpost-ferdigstilt.json");
		stubSafHentDokument(NOT_FOUND, "PRODUKSJON", APPLICATION_JSON_VALUE, "dokument-not-found.json");

		EntityExchangeResult<String> response = restTestClient.post()
				.uri("/Dokumentbehandling")
				.contentType(TEXT_XML)
				.body(fromClassPath("dokumentbehandling-hentdokument-request.xml"))
				.exchange()
				.expectStatus().is5xxServerError()
				.expectBody(String.class).returnResult();
		assertThat(response.getResponseBody()).contains("<faultstring xml:lang=\"en\">Finner ikke brev med brevreferanse 100000000</faultstring>");
	}

	private String fromClassPath(String path) throws IOException {
		return IOUtils.resourceToString("/" + path, UTF_8);
	}

	private void stubNaisTexas() {
		stubFor(post("/naistexas")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("naistexas/token-response.json")));
	}

	private void stubSafJournalpostQuery(String filename) {
		stubFor(post("/saf/graphql")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/" + filename)));
	}

	private void stubSafHentDokument(String variantFormat, String contentType) {
		stubFor(get("/saf/rest/hentdokument/100000000/100000000/" + variantFormat)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, contentType)
						.withBody(DOKUMENT)));
	}

	private void stubSafHentDokument(HttpStatus httpStatus, String variantFormat, String contentType, String filename) {
		stubFor(get("/saf/rest/hentdokument/100000000/100000000/" + variantFormat)
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, contentType)
						.withStatus(httpStatus.value())
						.withBodyFile("dokarkiv/" + filename)));
	}
}
