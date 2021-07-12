package no.nav.brevserver.core.repository;

import no.nav.brevserver.core.domain.entities.Brevtilgang;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, BrevtilgangRepository.class})
@ActiveProfiles("itest")
public class RepositoryTest {

	@Autowired
	private BrevtilgangRepository brevtilgangRepository;

	@Test
	public void testUpdate(){
		List<Brevtilgang> all = (List<Brevtilgang>) brevtilgangRepository.findAll();
	}

}
