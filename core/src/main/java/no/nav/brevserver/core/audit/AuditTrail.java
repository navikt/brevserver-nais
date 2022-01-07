package no.nav.brevserver.core.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.sql.Date;

@Embeddable
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrail {

	@Column(name = "OPPRETTET_AV")
	private String opprettetAv;
	@Column(name = "DATO_OPPRETTET")
	private Date opprettetDato;
	@Column(name = "ENDRET_AV")
	private String endretAv;
	@Column(name = "DATO_ENDRET")
	private Date endretDato;

}
