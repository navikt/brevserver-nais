package no.nav.brevserver.core.repository;

import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BrevRepository extends CrudRepository<Brev, BrevreferanseSystemCompositeId> {

}
