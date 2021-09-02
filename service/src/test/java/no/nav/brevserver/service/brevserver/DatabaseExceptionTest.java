package no.nav.brevserver.service.brevserver;

import no.nav.brevserver.builder.BrevStatusBuilder;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.domain.entities.Brevtilgang;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.repository.BrevSystemTilgangRepository;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.repository.BrevtilgangRepository;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.SysTilgangVO;
import no.nav.brevserver.service.AbstractDatabaseTest;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static no.nav.brevserver.builder.BrevStatusBuilder.getBrevStatusBuilder;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles("itest")
@Transactional
public class DatabaseExceptionTest  extends AbstractDatabaseTest {

	private static final String SYSTEM_PASSORD = "Pensjon123";
	private static final String TOKEN = "Token";

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
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldThrowExceptionIfSjekkSystemtilgangFailsBecauseOfWrongStatement() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();
		brevtilgangService.sjekkSystemTilgang(SYSTEM_ID, SYSTEM_PASSORD);
	}

	@Test
	public void shouldThrowExceptionIfHentBrevStatusFailsBecauseOfWrongStatement() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();
		brevstatusService.hentBrevStatus(BREVREFERANSE,SYSTEM_ID);
	}

	@Test
	public void shouldThrowExceptionIfLagreBrevStatusFailsBecauseOfWrongStatement() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();
		brevstatusService.lagreBrevStatus(defaultBrevStatus().build());
	}

	@Test
	public void shouldThrowExceptionIfLagreTilgangFailsBecauseOfWrongStatement() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();
		brevtilgangService.lagreTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);
	}


	@Test
	public void shouldReturnNullforSystilgangSomIkkeEksisterer() throws Exception {
		SysTilgangVO sysTilgang = brevtilgangService.hentTilgangUtenCache(SYSTEM_ID);

		assertThat(sysTilgang, nullValue());
	}

	@Test
	public void shouldThrowExceptionIfHentTilgangFailsBecauseOfWrongStatement() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();

		throwExceptionWhenQueryIsExecuted();
		brevtilgangService.hentTilgangUtenCache(SYSTEM_ID);
	}

	@Test
	public void shouldThrowExceptionForNotAllowedNullField() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();
		throwExceptionWhenQueryIsExecuted();
		BrevStatusVO invalidBrevStatus = defaultBrevStatus().brevreferanse(null).systemID(null).build();
		brevstatusService.lagreBrevStatus(invalidBrevStatus);
	}

	private void throwExceptionWhenQueryIsExecuted() throws Exception {
		when(brevSystemTilgangRepository.findBySysId(any(String.class))).thenThrow(new RuntimeException("Database nede"));
		when(brevstatusRepository.findById(any(BrevreferanseSystemCompositeId.class))).thenThrow(new RuntimeException("Database nede"));
		when(brevtilgangRepository.save(any(Brevtilgang.class))).thenThrow(new RuntimeException("Database nede"));
		when(brevstatusRepository.save(any(Brevstatus.class))).thenThrow(new RuntimeException("Database nede"));
	}

	private void expectExceptionDatabaseNoDatabaseTilgjengelig() {
		thrown.expect(BrevTechnicalException.class);
		thrown.expectMessage("Databasen til brevserveren er ikke tilgjengelig");
	}

	private BrevStatusBuilder defaultBrevStatus() {
		return getBrevStatusBuilder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID).returKoe(RETURKOE)
				.bestillerBrukerID(BESTILLER_ID).brevmal(BREVMAL).status(STATUS).format(FORMAT)
				.skrivertype(SKRIVERTYPE).skriver(SKRIVER).arkiver(ARKIVER).skuff(SKUFF);
	}
}
