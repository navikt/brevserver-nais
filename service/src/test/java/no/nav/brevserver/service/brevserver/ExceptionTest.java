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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ExceptionTest extends AbstractTest {

	@Autowired
	private BrevtilgangService brevtilgangService;
	@Autowired
	private BrevstatusService brevstatusService;
	@MockitoBean
	private BrevSystemTilgangRepository brevSystemTilgangRepository;
	@MockitoBean
	private BrevstatusRepository brevstatusRepository;
	@MockitoBean
	private BrevtilgangRepository brevtilgangRepository;

	@Test
	public void shouldThrowExceptionIfSjekkSystemtilgangFailsBecauseOfWrongStatement() {
		when(brevSystemTilgangRepository.findBySysId(any(String.class))).thenThrow(new RuntimeException("Database nede"));

		assertThatExceptionOfType(BrevTechnicalException.class)
				.isThrownBy(() -> brevtilgangService.sjekkSystemTilgang(SYSTEM_ID, SYSTEM_PASSORD))
				.withMessage("Databasen til brevserveren er ikke tilgjengelig");
	}

	@Test
	public void shouldThrowExceptionIfHentBrevStatusFailsBecauseOfWrongStatement() {
		when(brevstatusRepository.findById(any(BrevreferanseSystemCompositeId.class))).thenThrow(new RuntimeException("Database nede"));

		assertThatExceptionOfType(BrevTechnicalException.class)
				.isThrownBy(() -> brevstatusService.hentBrevStatus(BREVREFERANSE, SYSTEM_ID))
				.withMessage("Databasen til brevserveren er ikke tilgjengelig");
	}

	@Test
	public void shouldThrowExceptionIfLagreBrevStatusFailsBecauseOfWrongStatement() {
		when(brevstatusRepository.save(any(Brevstatus.class))).thenThrow(new RuntimeException("Database nede"));

		assertThatExceptionOfType(BrevTechnicalException.class)
				.isThrownBy(() -> brevstatusService.lagreBrevStatus(defaultBrevStatus()))
				.withMessage("Databasen til brevserveren er ikke tilgjengelig");
	}

	@Test
	public void shouldThrowExceptionIfLagreTilgangFailsBecauseOfWrongStatement() {
		when(brevtilgangRepository.save(any(Brevtilgang.class))).thenThrow(new RuntimeException("Database nede"));

		assertThatExceptionOfType(BrevTechnicalException.class)
				.isThrownBy(() -> brevtilgangService.lagreTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN))
				.withMessage("Databasen til brevserveren er ikke tilgjengelig");
	}

	@Test
	public void shouldReturnNullforSystilgangSomIkkeEksisterer() throws Exception {
		SysTilgangVO sysTilgang = brevtilgangService.hentTilgangUtenCache(SYSTEM_ID);

		assertThat(sysTilgang).isNull();
	}

	@Test
	public void shouldThrowExceptionIfHentTilgangFailsBecauseOfWrongStatement() {
		when(brevSystemTilgangRepository.findBySysId(any(String.class))).thenThrow(new RuntimeException("Database nede"));

		assertThatExceptionOfType(BrevTechnicalException.class)
				.isThrownBy(() -> brevtilgangService.hentTilgangUtenCache(SYSTEM_ID))
				.withMessage("Databasen til brevserveren er ikke tilgjengelig");
	}

	@Test
	public void shouldThrowExceptionForNotAllowedNullField() {
		when(brevstatusRepository.findById(any(BrevreferanseSystemCompositeId.class))).thenThrow(new RuntimeException("Database nede"));

		BrevStatusVO invalidBrevStatus = defaultBrevStatus().toBuilder().brevreferanse(null).systemID(null).build();

		assertThatExceptionOfType(BrevTechnicalException.class)
				.isThrownBy(() -> brevstatusService.lagreBrevStatus(invalidBrevStatus))
				.withMessage("Databasen til brevserveren er ikke tilgjengelig");
	}

	private BrevStatusVO defaultBrevStatus() {
		return BrevStatusVO.builder()
				.brevreferanse(BREVREFERANSE)
				.systemID(SYSTEM_ID)
				.returKoe(RETURKOE)
				.bestillerBrukerID(BESTILLER_ID)
				.brevmal(BREVMAL)
				.status(STATUS)
				.format(FORMAT)
				.skrivertype(SKRIVERTYPE)
				.skriver(SKRIVER)
				.arkiver(ARKIVER)
				.skuff(SKUFF)
				.build();
	}

}