package no.nav.brevserver.core.audit;

import java.io.Serializable;

public interface Auditable extends Serializable {
	public AuditTrail getAuditTrail();
}
