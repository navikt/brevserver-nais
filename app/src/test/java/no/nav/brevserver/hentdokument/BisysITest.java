package no.nav.brevserver.hentdokument;

import no.nav.brevserver.ApplicationTestConfig;
import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.repository.BrevRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.function.Consumer;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.transaction.TestTransaction.end;
import static org.springframework.test.context.transaction.TestTransaction.flagForCommit;
import static org.springframework.test.context.transaction.TestTransaction.start;

@Transactional
@SpringBootTest(classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT)
@AutoConfigureTestDatabase
@AutoConfigureWebTestClient
@ActiveProfiles("itest")
public class BisysITest extends AbstractOauth2Test {
	private static final String BISYS_SYSTEMID = "BI12";

	private static final String BISYS_SCOPE = "bisys defaultaccess";

	private static final String HENTDOKUMENT_BISYS_URL = "/rest/hentdokument/bisys/{dokid}";

	@Autowired
	WebTestClient webTestClient;

	@Autowired
	BrevRepository brevRepository;

	@Test
	void skalHenteBisysdokument() {
		var dokId = "BIF123";
		var referanse = new BrevreferanseSystemCompositeId(dokId, BISYS_SYSTEMID);
		var bilag = new Brev(referanse, "status", "application/pdf", "brukerId", "brevdata".getBytes(), Timestamp.valueOf(now()));
		brevRepository.save(bilag);
		commitAndBeginNewTransaction();

		var response = webTestClient.get()
				.uri(HENTDOKUMENT_BISYS_URL, dokId)
				.headers(authHeader())
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.returnResult()
				.getResponseBody();

		assertThat(response).isEqualTo("brevdata");
	}

	@ParameterizedTest
	@ValueSource(strings = {"BIF123456789012345678901234567890", "123", "BIF", "BIFabc", "BIF-1", "a", " "})
	void skalReturnereBadRequestForUgyldigDokId(String dokId) {

		webTestClient.get()
				.uri(HENTDOKUMENT_BISYS_URL, dokId)
				.headers(authHeader())
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void skalReturnereNotFoundHvisSystemIdIkkeErBisys() {
		var dokId = "BIF456";
		var systemId = "FS22";

		var referanse = new BrevreferanseSystemCompositeId(dokId, systemId);
		var bilag = new Brev(referanse, "status", "application/pdf", "brukerId", "brevdata".getBytes(), Timestamp.valueOf(now()));
		brevRepository.save(bilag);
		commitAndBeginNewTransaction();

		webTestClient.get()
				.uri(HENTDOKUMENT_BISYS_URL, dokId)
				.headers(authHeader())
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void skalReturnereNotFoundHvisDokumentIkkeFinnes() {
		webTestClient.get()
				.uri(HENTDOKUMENT_BISYS_URL, "BIF456")
				.headers(authHeader())
				.exchange()
				.expectStatus().isNotFound();
	}

	@ParameterizedTest
	@ValueSource(strings = {"application/msword.docx", "text/rtf"})
	void skalReturnereNotFoundHvisDokumenttypenIkkeErPdf(String contentType) {
		var dokId = "BIF789";
		var referanse = new BrevreferanseSystemCompositeId(dokId, BISYS_SYSTEMID);
		var bilag = new Brev(referanse, "status", contentType, "brukerId", "brevdata".getBytes(), Timestamp.valueOf(now()));
		brevRepository.save(bilag);
		commitAndBeginNewTransaction();

		webTestClient.get()
				.uri(HENTDOKUMENT_BISYS_URL, dokId)
				.headers(authHeader())
				.exchange()
				.expectStatus().isNotFound();
	}

	@ParameterizedTest
	@ValueSource(strings = {"ugyldig", "BISYS", "OEBS", "biys", "foo"})
	void skalReturnereNotFoundNaarSystemHverkenErOebsEllerBisys(String system) {
		var hentDokumentUrl = "/rest/hentdokument/{system}/{dokid}";

		webTestClient.get()
				.uri(hentDokumentUrl, system, "BIF123")
				.headers(authHeader())
				.exchange()
				.expectStatus().isNotFound()
				.expectBody(String.class)
				.isEqualTo("\"System %s not found. Must be oebs or bisys\"".formatted(system));
	}

	@ParameterizedTest
	@ValueSource(strings = {"ikke-oebs", "ikke-bisys", "defaultaccess"})
	void skalReturnereUnauthorizedForUgyldigScope(String scope) {
		webTestClient.get()
				.uri(HENTDOKUMENT_BISYS_URL, "BIF123")
				.headers(authHeader(scope))
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void skalHenteBisysdokumentMedMaskinTilMaskinTokenMedBisysRole() {
		var dokId = "BIF123";
		var referanse = new BrevreferanseSystemCompositeId(dokId, BISYS_SYSTEMID);
		var bilag = new Brev(referanse, "status", "application/pdf", "brukerId", "brevdata".getBytes(), Timestamp.valueOf(now()));
		brevRepository.save(bilag);
		commitAndBeginNewTransaction();

		var response = webTestClient.get()
				.uri(HENTDOKUMENT_BISYS_URL, dokId)
				.headers(headers -> headers.setBearerAuth(jwtMachinToMachine("bisys")))
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.returnResult()
				.getResponseBody();

		assertThat(response).isEqualTo("brevdata");
	}

	@ParameterizedTest
	@ValueSource(strings = {"ikke-oebs", "ikke-bisys", "defaultaccess"})
	void skalReturnereUnauthorizedForUgyldigRolleMedMaskinTilMaskinToken(String role) {
		webTestClient.get()
				.uri(HENTDOKUMENT_BISYS_URL, "BIF123")
				.headers(headers -> headers.setBearerAuth(jwtMachinToMachine(role)))
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void skalReturnereUnauthorizedHvisScopeMangler() {
		webTestClient.get()
				.uri(HENTDOKUMENT_BISYS_URL, "BIF123")
				.headers(headers -> headers.setBearerAuth(jwt()))
				.exchange()
				.expectStatus().isUnauthorized();
	}

	private Consumer<HttpHeaders> authHeader() {
		return authHeader(BISYS_SCOPE);
	}

	private Consumer<HttpHeaders> authHeader(String scope) {
		return headers -> headers.setBearerAuth(jwt(scope));
	}

	private static void commitAndBeginNewTransaction() {
		flagForCommit();
		end();
		start();
	}
}
