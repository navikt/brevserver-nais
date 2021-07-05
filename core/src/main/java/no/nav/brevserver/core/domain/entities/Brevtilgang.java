package no.nav.brevserver.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_BREVTILGANG")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Brevtilgang {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brevtilgang_seq")
	@GenericGenerator(name = "brevtilgang_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@Parameter(name = "sequence_name", value = "T_BREVTILGANG_SEQ"),
			@Parameter(name = "initial_value", value = "200000000")})
	@Column(name = "ID", nullable = false)
	private Long journalpostId;

	@Column(name = "TIMESTAMP")
	private LocalDateTime opprettetDato;

 	@Column(name = "TOKEN")
	private String token;

	@Column(name = "SYSTEMID")
	private String systemId;

	@Column(name = "BREVREFERANSE")
	private String brevreferanse;

}
