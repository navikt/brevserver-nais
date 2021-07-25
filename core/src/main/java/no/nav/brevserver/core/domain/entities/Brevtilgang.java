package no.nav.brevserver.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_BREVTILGANG")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Brevtilgang {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long journalpostId;

	@Column(name = "TIMESTAMP")
	private Timestamp opprettetDato;

 	@Column(name = "TOKEN")
	private String token;

	@Column(name = "SYSTEMID")
	private String systemId;

	@Column(name = "BREVREFERANSE")
	private String brevreferanse;

}
