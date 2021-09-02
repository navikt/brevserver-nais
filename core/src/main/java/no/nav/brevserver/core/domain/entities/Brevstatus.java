package no.nav.brevserver.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
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

	@Column(name = "RETURKOE")
	private String returKoe;

	@Column(name = "BESTILLERBRUKERID")
	private String bestillerBrukerID;

	@Column(name = "BREVMAL")
	private String brevmal;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "FORMAT")
	private String format;

	@Column(name = "SKRIVERTYPE")
	private String skrivertype;

	@Column(name = "SKRIVER")
	private String skriver;

	@Column(name = "ARKIVER")
	private String arkiver;

	@Column(name = "SKUFF")
	private String skuff;

	@Column(name = "TIMESTAMP")
	private Timestamp endret;

}
