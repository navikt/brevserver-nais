package no.nav.brevserver.service.brevlager.beans;

import no.nav.brevserver.core.repository.BrevRepository;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.service.AbstractDatabaseTest;
import no.nav.brevserver.service.BrevlagerService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("itest")
public class DatabaseExceptionTest extends AbstractDatabaseTest {

	@MockBean
	private BrevRepository brevRepository;
	@MockBean
	private BrevstatusRepository brevstatusRepository;
	@Autowired
	private BrevlagerService brevlagerService;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldThrowExceptionForFailedQueryInFerdigstillBrev() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();
		throwExceptionWhenQueryIsExecuted();
		brevlagerService.ferdigstillBrev(BrevStatusVO.builder().systemID("123").brevreferanse("123").token("123").build(), defaultBrev().build(), defaultBrev().build());
	}

	@Test
	public void shouldThrowExceptionForFailedQueryInLagreBrev() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();
		throwExceptionWhenQueryIsExecuted();
		brevlagerService.lagreBrev(defaultBrev().build(), new BrevStatusVO());
	}

	@Test
	public void shouldThrowExceptionForFailedQueryInGetBrev() throws Exception {
		expectExceptionDatabaseNoDatabaseTilgjengelig();
		throwExceptionWhenQueryIsExecuted();
		brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE);
	}

	private void expectExceptionDatabaseNoDatabaseTilgjengelig() {
		thrown.expect(BrevTechnicalException.class);
		thrown.expectMessage("Databasen til brevserveren er ikke tilgjengelig");
	}

	private void throwExceptionWhenQueryIsExecuted() throws Exception {
		when(brevRepository.findById(any())).thenThrow(new RuntimeException("Database nede"));
		when(brevstatusRepository.save(any())).thenThrow(new RuntimeException("Database nede"));
	}
}
