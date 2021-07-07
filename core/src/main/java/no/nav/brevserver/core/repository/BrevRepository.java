package no.nav.brevserver.core.repository;

import no.nav.brevserver.core.domain.entities.Brev;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BrevRepository extends CrudRepository<Brev, String> {

	List<Brev> findBySystemIdAndBrevreferanse(String systemId, String brevreferanse);
}
