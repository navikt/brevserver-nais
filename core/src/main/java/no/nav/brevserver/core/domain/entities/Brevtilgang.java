package no.nav.brevserver.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import static no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId.BREVREFERANSE_LENGTH;
import static no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId.SYSTEMID_LENGTH;

@Entity
@Table(name = "T_BREVTILGANG")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Brevtilgang {

	private static final String BREVTILGANG_SEQ = "brevtilgang_seq";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = BREVTILGANG_SEQ)
	@SequenceGenerator(name = BREVTILGANG_SEQ, sequenceName = BREVTILGANG_SEQ, allocationSize = 1)
	@Column(name = "ID", nullable = false)
	private Long journalpostId;

 	@Column(name = "TOKEN", length = 64)
	private String token;

	@Column(name = "SYSTEMID", length = SYSTEMID_LENGTH)
	private String systemId;

	@Column(name = "BREVREFERANSE", length = BREVREFERANSE_LENGTH)
	private String brevreferanse;

	@Column(name = "TIMESTAMP")
	private Timestamp endret;

}
