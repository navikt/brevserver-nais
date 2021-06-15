package no.nav.brevserver.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_BREVSTATUS")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Brevstatus {

	private static final long serialVersionUID = 7514533773610729921L;

	@Id
	@Column(name = "BREVREFERANSE")
	private String brevreferanse;

	@Column(name = "SYSTEM_ID")
	private String systemID;

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

}
