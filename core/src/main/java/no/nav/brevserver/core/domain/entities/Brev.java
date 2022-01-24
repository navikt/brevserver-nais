package no.nav.brevserver.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.audit.AuditTrail;
import no.nav.brevserver.core.audit.Auditable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "T_BREVLAGER_X")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Brev {

	@EmbeddedId
	private BrevreferanseSystemCompositeId id;

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

/*
	@Embedded
	@Setter
	private AuditTrail auditTrail;
*/
}
