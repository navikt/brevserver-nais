package no.nav.brevserver.core.utils.stelvio;

import no.nav.brevserver.core.exception.OperationalException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class RequestContextSetter {
	private static Field requestContextHolder;

	protected RequestContextSetter() {
	}

	public static void setRequestContext(RequestContext requestContext) {
		try {
			((RedeployableThreadLocalSubstitute) getRequestContextHolder().get(null)).set(requestContext);
			MDCOperations.setMdcProperties();
		} catch (Exception var2) {
			throw new OperationalException("Setting requestContext on RequestContextHolder failed.", var2);
		}
	}

	private static synchronized Field getRequestContextHolder() {
		if (requestContextHolder == null) {
			requestContextHolder = ReflectionUtils.findField(RequestContextHolder.class, "REQUEST_CONTEXT_HOLDER");
			requestContextHolder.setAccessible(true);
		}

		return requestContextHolder;
	}
}
