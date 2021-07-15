package no.nav.brevserver.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "T_BREVLAGER5")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Brev {

	//TODO: Fix composite ID from brevreferanse og systemId
	@Id
	@Column(name = "BREVREFERANSE")
	private String brevreferanse;

	@Column(name = "SYSTEMID")
	private String systemId;

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
