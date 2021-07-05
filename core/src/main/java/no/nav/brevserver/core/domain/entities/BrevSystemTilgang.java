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

@Entity
@Table(name = "T_BREVSYSTILGANG")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BrevSystemTilgang {

	@Id
	@Column(name = "systemid")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brevsystilgang_seq")
	@GenericGenerator(name = "brevsystilgang_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@Parameter(name = "sequence_name", value = "T_BREVSYSTEMTILGANG_SEQ"),
			@Parameter(name = "initial_value", value = "1")})
	private String sysId;

	@Column(name = "systempassord")
	private String pwd;
}
