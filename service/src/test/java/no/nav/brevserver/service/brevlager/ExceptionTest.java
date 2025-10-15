package no.nav.brevserver.service.brevlager;

import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.repository.BrevRepository;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.config.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ExceptionTest extends AbstractTest {

	@MockitoBean
	private BrevRepository brevRepository;
	@MockitoBean
	private BrevstatusRepository brevstatusRepository;
	@Autowired
	private BrevlagerService brevlagerService;

	@BeforeEach
	void setUp() {
		when(brevRepository.findById(any())).thenThrow(new RuntimeException("Database nede"));
		when(brevstatusRepository.save(any())).thenThrow(new RuntimeException("Database nede"));
	}

	@Test
	public void shouldThrowExceptionForFailedQueryInFerdigstillBrev() {
		assertThatExceptionOfType(BrevTechnicalException.class)
				.isThrownBy(() -> brevlagerService.ferdigstillBrev(BrevStatusVO.builder().systemID("123").brevreferanse("123").token("123").build(), defaultBrev().build(), defaultBrev().build()))
				.withMessage("Databasen til brevserveren er ikke tilgjengelig");
	}

	@Test
	public void shouldThrowExceptionForFailedQueryInLagreBrev() {
		assertThatExceptionOfType(BrevTechnicalException.class)
				.isThrownBy(() -> brevlagerService.lagreBrev(defaultBrev().build(), new BrevStatusVO()))
				.withMessage("Databasen til brevserveren er ikke tilgjengelig");
	}

	@Test
	public void shouldThrowExceptionForFailedQueryInGetBrev() {
		assertThatExceptionOfType(BrevTechnicalException.class)
				.isThrownBy(() -> brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE))
				.withMessage("Databasen til brevserveren er ikke tilgjengelig");
	}

}