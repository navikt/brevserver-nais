package no.nav.brevserver.core.repository;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BrevstatusRepository extends CrudRepository<Brevstatus, Long> {

	public List<Brevstatus> findByBrevreferanseAndSystemID(String brevreferanse, String SystemId);
}
