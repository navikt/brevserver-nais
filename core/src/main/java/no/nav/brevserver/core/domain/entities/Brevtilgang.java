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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.sql.Timestamp;

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
//	@GenericGenerator(name = "brevtilgang_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
//			@Parameter(name = "sequence_name", value = "BREVTILGANG_SEQ"),
//			@Parameter(name = "initial_value", value = "1")})
	@Column(name = "ID", nullable = false)
	private Long journalpostId;

 	@Column(name = "TOKEN")
	private String token;

	@Column(name = "SYSTEMID")
	private String systemId;

	@Column(name = "BREVREFERANSE")
	private String brevreferanse;

	@Column(name = "TIMESTAMP")
	private Timestamp endret;

}
