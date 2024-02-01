package no.nav.brevserver.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
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

	@Column(name = "STATUS")
	private String status;

	@Column(name = "CONTENTTYPE")
	private String contentType;

	@Column(name = "BRUKERID")
	private String brukerId;

	@Column(name = "BREVDATA")
	@Lob
	private byte[] brevdata;

	@Column(name = "TIMESTAMP")
	private Timestamp endret;
}
