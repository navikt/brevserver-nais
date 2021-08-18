package no.nav.brevserver.core.domain.entities.id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BrevreferanseSystemCompositeId implements Serializable {

	@Column(name = "BREVREFERANSE")
	private String brevreferanse;

	@Column(name = "SYSTEMID")
	private String systemId;
}
