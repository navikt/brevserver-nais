package no.nav.brevserver.service.brevserver;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.domain.entities.Brevtilgang;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.repository.BrevSystemTilgangRepository;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.repository.BrevtilgangRepository;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.SysTilgangVO;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import no.nav.brevserver.service.config.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ExceptionTest extends AbstractTest {

	@Autowired
	private BrevtilgangService brevtilgangService;
	@Autowired
	private BrevstatusService brevstatusService;
	@MockBean
	private BrevSystemTilgangRepository brevSystemTilgangRepository;
	@MockBean
	private BrevstatusRepository brevstatusRepository;
	@MockBean
	private BrevtilgangRepository brevtilgangRepository;

	@Test
	public void shouldThrowExceptionIfSjekkSystemtilgangFailsBecauseOfWrongStatement() throws Exception {
		throwExceptionWhenQueryIsExecuted();
		var e = assertThrows(BrevTechnicalException.class, () -> brevtilgangService.sjekkSystemTilgang(SYSTEM_ID, SYSTEM_PASSORD));

		assertEquals("Databasen til brevserveren er ikke tilgjengelig", e.getMessage());
	}

	@Test
	public void shouldThrowExceptionIfHentBrevStatusFailsBecauseOfWrongStatement() throws Exception {
		throwExceptionWhenQueryIsExecuted();

		var e = assertThrows(BrevTechnicalException.class, () -> brevstatusService.hentBrevStatus(BREVREFERANSE, SYSTEM_ID));

		assertEquals("Databasen til brevserveren er ikke tilgjengelig", e.getMessage());
	}

	@Test
	public void shouldThrowExceptionIfLagreBrevStatusFailsBecauseOfWrongStatement() throws Exception {
		throwExceptionWhenQueryIsExecuted();

		var e = assertThrows(BrevTechnicalException.class, () -> brevstatusService.lagreBrevStatus(defaultBrevStatus()));

		assertEquals("Databasen til brevserveren er ikke tilgjengelig", e.getMessage());
	}

	@Test
	public void shouldThrowExceptionIfLagreTilgangFailsBecauseOfWrongStatement() throws Exception {
		throwExceptionWhenQueryIsExecuted();

		var e = assertThrows(BrevTechnicalException.class, () -> brevtilgangService.lagreTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN));

		assertEquals("Databasen til brevserveren er ikke tilgjengelig", e.getMessage());
	}

	@Test
	public void shouldReturnNullforSystilgangSomIkkeEksisterer() throws Exception {
		SysTilgangVO sysTilgang = brevtilgangService.hentTilgangUtenCache(SYSTEM_ID);

		assertThat(sysTilgang, nullValue());
	}

	@Test
	public void shouldThrowExceptionIfHentTilgangFailsBecauseOfWrongStatement() throws Exception {
		throwExceptionWhenQueryIsExecuted();

		var e = assertThrows(BrevTechnicalException.class, () -> brevtilgangService.hentTilgangUtenCache(SYSTEM_ID));

		assertEquals("Databasen til brevserveren er ikke tilgjengelig", e.getMessage());
	}

	@Test
	public void shouldThrowExceptionForNotAllowedNullField() throws Exception {
		throwExceptionWhenQueryIsExecuted();

		BrevStatusVO invalidBrevStatus = defaultBrevStatus().toBuilder().brevreferanse(null).systemID(null).build();
		var e = assertThrows(BrevTechnicalException.class, () -> brevstatusService.lagreBrevStatus(invalidBrevStatus));

		assertEquals("Databasen til brevserveren er ikke tilgjengelig", e.getMessage());
	}

	private void throwExceptionWhenQueryIsExecuted() throws Exception {
		when(brevSystemTilgangRepository.findBySysId(any(String.class))).thenThrow(new RuntimeException("Database nede"));
		when(brevstatusRepository.findById(any(BrevreferanseSystemCompositeId.class))).thenThrow(new RuntimeException("Database nede"));
		when(brevtilgangRepository.save(any(Brevtilgang.class))).thenThrow(new RuntimeException("Database nede"));
		when(brevstatusRepository.save(any(Brevstatus.class))).thenThrow(new RuntimeException("Database nede"));
	}

	private BrevStatusVO defaultBrevStatus() {
		return BrevStatusVO.builder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID).returKoe(RETURKOE)
				.bestillerBrukerID(BESTILLER_ID).brevmal(BREVMAL).status(STATUS).format(FORMAT)
				.skrivertype(SKRIVERTYPE).skriver(SKRIVER).arkiver(ARKIVER).skuff(SKUFF).build();
	}
}
