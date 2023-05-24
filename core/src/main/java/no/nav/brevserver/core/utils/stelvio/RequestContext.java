package no.nav.brevserver.core.utils.stelvio;

public interface RequestContext {
	String getScreenId();

	String getModuleId();

	String getProcessId();

	String getTransactionId();

	String getComponentId();

	String getUserId();
}
