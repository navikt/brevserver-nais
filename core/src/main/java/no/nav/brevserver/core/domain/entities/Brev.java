package no.nav.brevserver.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;

import java.sql.Timestamp;

@Entity
@Table(name = "T_BREVLAGER_X")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Brev {

	@EmbeddedId
	private BrevreferanseSystemCompositeId id;

	@Column(name = "STATUS", length = 8, nullable = false)
	private String status;

	@Column(name = "CONTENTTYPE", length = 64)
	private String contentType;

	@Column(name = "BRUKERID", length = 18)
	private String brukerId;

	@Column(name = "BREVDATA", nullable = false)
	@Lob
	private byte[] brevdata;

	@Column(name = "TIMESTAMP", nullable = false)
	private Timestamp endret;
}
