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
	@Column(name = "systemid", nullable = false)
	private String sysId;

	@Column(name = "systempassord")
	private String pwd;
}
