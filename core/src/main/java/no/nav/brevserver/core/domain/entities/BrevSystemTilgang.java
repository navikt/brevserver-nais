package no.nav.brevserver.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId.SYSTEMID_LENGTH;

@Entity
@Table(name = "T_BREVSYSTILGANG")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BrevSystemTilgang {

	@Id
	@Column(name = "systemid", length = SYSTEMID_LENGTH, nullable = false)
	private String sysId;

	@Column(name = "systempassord", length = 32, nullable = false)
	private String pwd;
}
