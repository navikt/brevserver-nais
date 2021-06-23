package no.nav.brevserver.repository;

import no.nav.brevserver.core.domain.entities.BrevSystemTilgang;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BrevSystemTilgangRepository extends CrudRepository<BrevSystemTilgang, Long> {

	public List<BrevSystemTilgang> findBySysId(String systemId);
}
