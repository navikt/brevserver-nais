package no.nav.brevserver.core.utils.stelvio;

public final class RequestContextHolder {
	private static final RedeployableThreadLocalSubstitute<RequestContext> REQUEST_CONTEXT_HOLDER = new RedeployableThreadLocalSubstitute();

	private RequestContextHolder() {
	}

	public static RequestContext currentRequestContext() throws IllegalStateException {
		RequestContext context = REQUEST_CONTEXT_HOLDER.get();
		if (context == null) {
			throw new IllegalStateException("No thread-bound request context found: Make sure filters for binding the request is setup properly for the web application in front and the proper request context interceptors are setup for the layers below.");
		} else {
			return context;
		}
	}

	public static boolean isRequestContextSet() {
		return REQUEST_CONTEXT_HOLDER.isValueSet();
	}
}
