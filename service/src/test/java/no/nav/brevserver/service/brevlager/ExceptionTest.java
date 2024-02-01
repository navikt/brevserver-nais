package no.nav.brevserver.service.brevlager;

import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.repository.BrevRepository;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.config.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ExceptionTest extends AbstractTest {

	@MockBean
	private BrevRepository brevRepository;
	@MockBean
	private BrevstatusRepository brevstatusRepository;
	@Autowired
	private BrevlagerService brevlagerService;

	@Test
	public void shouldThrowExceptionForFailedQueryInFerdigstillBrev() throws Exception {
		throwExceptionWhenQueryIsExecuted();
		var e = assertThrows(BrevTechnicalException.class, () ->
				brevlagerService.ferdigstillBrev(BrevStatusVO.builder().systemID("123").brevreferanse("123").token("123").build(), defaultBrev().build(), defaultBrev().build()));

		assertThat(e.getMessage()).isEqualTo("Databasen til brevserveren er ikke tilgjengelig");
	}

	@Test
	public void shouldThrowExceptionForFailedQueryInLagreBrev() throws Exception {
		throwExceptionWhenQueryIsExecuted();

		var e = assertThrows(BrevTechnicalException.class, () ->
				brevlagerService.lagreBrev(defaultBrev().build(), new BrevStatusVO()));

		assertThat(e.getMessage()).isEqualTo("Databasen til brevserveren er ikke tilgjengelig");
	}

	@Test
	public void shouldThrowExceptionForFailedQueryInGetBrev() throws Exception {
		throwExceptionWhenQueryIsExecuted();

		var e = assertThrows(BrevTechnicalException.class, () ->
				brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE));

		assertThat(e.getMessage()).isEqualTo("Databasen til brevserveren er ikke tilgjengelig");
	}

	private void throwExceptionWhenQueryIsExecuted() throws Exception {
		when(brevRepository.findById(any())).thenThrow(new RuntimeException("Database nede"));
		when(brevstatusRepository.save(any())).thenThrow(new RuntimeException("Database nede"));
	}
}
