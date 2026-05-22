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
import static no.nav.brevserver.hentdokument.HentDokumentController.OEBS_SYSTEMID;
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
public class BilagITest extends AbstractOauth2Test {

	private static final String HENTDOKUMENT_URL = "/rest/hentdokument/";

	@Autowired
	WebTestClient webTestClient;

	@Autowired
	BrevRepository brevRepository;

	@Test
	void skalHenteBilagsdokument() {
		var dokId = "123";
		var referanse = new BrevreferanseSystemCompositeId(dokId, OEBS_SYSTEMID);
		var bilag = new Brev(referanse, "status", "application/pdf", "brukerId", "brevdata".getBytes(), Timestamp.valueOf(now()));
		brevRepository.save(bilag);
		commitAndBeginNewTransaction();

		var response = webTestClient.get()
				.uri(HENTDOKUMENT_URL + dokId)
				.headers(authHeader())
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.returnResult()
				.getResponseBody();

		assertThat(response).isEqualTo("brevdata");
	}

	@ParameterizedTest
	@ValueSource(strings = {"123456789012345678901234567890123", "-1", "a", " "})
	void skalReturnereBadRequestForUgyldigDokId(String dokId) {

		webTestClient.get()
				.uri(HENTDOKUMENT_URL + dokId)
				.headers(authHeader())
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void skalReturnereNotFoundHvisSystemIdIkkeErOebs() {
		var dokId = "456";
		var systemId = "FS22";

		var referanse = new BrevreferanseSystemCompositeId(dokId, systemId);
		var bilag = new Brev(referanse, "status", "application/pdf", "brukerId", "brevdata".getBytes(), Timestamp.valueOf(now()));
		brevRepository.save(bilag);
		commitAndBeginNewTransaction();

		webTestClient.get()
				.uri(HENTDOKUMENT_URL + dokId)
				.headers(authHeader())
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void skalReturnereNotFoundHvisDokumentIkkeFinnes() {
		var dokId = "456";

		webTestClient.get()
				.uri(HENTDOKUMENT_URL + dokId)
				.headers(authHeader())
				.exchange()
				.expectStatus().isNotFound();
	}

	@ParameterizedTest
	@ValueSource(strings = {"application/msword.docx", "text/rtf"})
	void skalReturnereNotFoundHvisDokumenttypenIkkeErPdf(String contentType) {
		var dokId = "789";
		var referanse = new BrevreferanseSystemCompositeId(dokId, OEBS_SYSTEMID);
		var bilag = new Brev(referanse, "status", contentType, "brukerId", "brevdata".getBytes(), Timestamp.valueOf(now()));
		brevRepository.save(bilag);
		commitAndBeginNewTransaction();

		webTestClient.get()
				.uri(HENTDOKUMENT_URL + dokId)
				.headers(authHeader())
				.exchange()
				.expectStatus().isNotFound();
	}

	private Consumer<HttpHeaders> authHeader() {
		return headers -> headers.setBearerAuth(jwt());
	}

	private static void commitAndBeginNewTransaction() {
		flagForCommit();
		end();
		start();
	}
}
