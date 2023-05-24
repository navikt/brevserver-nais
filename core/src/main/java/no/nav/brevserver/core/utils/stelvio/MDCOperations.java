package no.nav.brevserver.core.utils.stelvio;

import org.slf4j.MDC;

public class MDCOperations {
	public MDCOperations() {
	}

	public static void setMdcProperties() {
		if (RequestContextHolder.isRequestContextSet()) {
			RequestContext requestContext = RequestContextHolder.currentRequestContext();
			if (requestContext.getComponentId() != null) {
				MDC.put("applicationKey", requestContext.getComponentId());
			}

			if (requestContext.getScreenId() != null) {
				MDC.put("screen", requestContext.getScreenId());
			}

			if (requestContext.getUserId() != null) {
				MDC.put("user", requestContext.getUserId());
			}

			if (requestContext.getTransactionId() != null) {
				MDC.put("transaction", requestContext.getTransactionId());
			}

			if (requestContext.getProcessId() != null) {
				MDC.put("process", requestContext.getProcessId());
			}

			if (requestContext.getModuleId() != null) {
				MDC.put("module", requestContext.getModuleId());
			}
		}

	}

}
