package no.nav.brevserver.repository;

import no.nav.brevserver.core.domain.entities.Brevtilgang;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BrevtilgangRepository extends CrudRepository<Brevtilgang, Long> {

	List<Brevtilgang> findBySystemIdAndBrevreferanse(String systemId, String brevreferanse);

}
