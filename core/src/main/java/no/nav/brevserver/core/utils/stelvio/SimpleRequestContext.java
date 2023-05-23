package no.nav.brevserver.core.utils.stelvio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleRequestContext implements RequestContext, Serializable {
	@Serial
	private static final long serialVersionUID = 332477076847471488L;
	private String screenId;
	private String moduleId;
	private String transactionId;
	private String componentId;
	private String userId;
	private String processId;
}
