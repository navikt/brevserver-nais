package no.nav.brevserver.core.repository;

import no.nav.brevserver.core.domain.entities.BrevSystemTilgang;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BrevSystemTilgangRepository extends CrudRepository<BrevSystemTilgang, Long> {

	List<BrevSystemTilgang> findBySysId(String systemId);
}
