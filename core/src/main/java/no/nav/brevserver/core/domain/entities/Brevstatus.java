package no.nav.brevserver.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;

import java.sql.Timestamp;

@Entity
@Table(name = "T_BREVSTATUS")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Brevstatus {

	@EmbeddedId
	private BrevreferanseSystemCompositeId id;

	@Column(name = "RETURKOE", length = 256)
	private String returKoe;

	@Column(name = "BESTILLERBRUKERID", length = 100)
	private String bestillerBrukerID;

	@Column(name = "BREVMAL")
	private String brevmal;

	@Column(name = "STATUS", length = 8, nullable = false)
	private String status;

	@Column(name = "FORMAT", length = 32)
	private String format;

	@Column(name = "SKRIVERTYPE", length = 16)
	private String skrivertype;

	@Column(name = "SKRIVER", length = 32)
	private String skriver;

	@Column(name = "ARKIVER", length = 3)
	private String arkiver;

	@Column(name = "SKUFF", length = 3)
	private String skuff;

	@Column(name = "TIMESTAMP", nullable = false)
	private Timestamp endret;
}
