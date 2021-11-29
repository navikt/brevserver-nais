package no.nav.brevserver.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.brevserver.core.audit.AuditTrail;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "BREVLAGER_HISTORIKK")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BrevlagerHistorikk {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brevlagerhistorikk_seq")
	@GenericGenerator(name = "brevlagerhistorikk_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@Parameter(name = "sequence_name", value = "BREVLAGER_HISTORIKK_SEQ"),
			@Parameter(name = "initial_value", value = "1")})
	@Column(name = "BREVLAGER_HISTORIK_ID")
	private Long id;

	@Column(name = "BREVREFERANSE")
	private String brevreferanse;

	@Column(name = "SYSTEMID")
	private String systemId;

	@Column(name = "BRUKERID")
	private String brukerId;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "CONTENTTYPE")
	private String contentType;

	@Column(name = "BREVDATA")
	@Lob
	private byte[] brevdata;

	@Column(name = "VASKET")
	private Character vasket;

	@Embedded
	@Setter
	private AuditTrail auditTrail;

}
