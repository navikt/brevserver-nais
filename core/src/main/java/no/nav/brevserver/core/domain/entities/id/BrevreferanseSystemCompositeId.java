package no.nav.brevserver.core.domain.entities.id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
public class BrevreferanseSystemCompositeId implements Serializable {

	public static final int BREVREFERANSE_LENGTH = 32;
	public static final int SYSTEMID_LENGTH = 4;

	@Column(name = "BREVREFERANSE", length = BREVREFERANSE_LENGTH)
	private String brevreferanse;

	@Column(name = "SYSTEMID", length = SYSTEMID_LENGTH)
	private String systemId;
}
