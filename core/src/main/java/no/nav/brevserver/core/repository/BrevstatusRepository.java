package no.nav.brevserver.core.repository;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import org.springframework.data.repository.CrudRepository;

public interface BrevstatusRepository extends CrudRepository<Brevstatus, BrevreferanseSystemCompositeId> {

}
