package config;

import no.nav.brevserver.core.repository.BrevSystemTilgangRepository;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.repository.BrevtilgangRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT)
@ActiveProfiles("itest")
public class AbstractTest {
	@Autowired
	protected BrevtilgangRepository brevtilgangRepository;

	@Autowired
	protected BrevSystemTilgangRepository brevSystemTilgangRepository;

	@Autowired
	protected BrevstatusRepository brevstatusRepository;

}