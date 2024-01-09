package no.nav.brevserver.hentdokument.mdc;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@Slf4j
public class MDCInterceptor implements HandlerInterceptor {

	public static final String MDC_USER_ID = "userId";
	public static final String MDC_CALL_ID = "callId";

	public static final String NAV_CALLID = "Nav-Callid";
	public static final String NAV_USER_ID = "Nav-User-Id";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		populateCallId(request);
		populateUserId(request);

		return true;
	}

	private static void populateCallId(HttpServletRequest request) {
		String callId = request.getHeader(NAV_CALLID);
		if (isEmpty(callId)) {
			callId = UUID.randomUUID().toString();
		}
		MDC.put(MDC_CALL_ID, callId);
	}

	private static void populateUserId(HttpServletRequest request) {
		final String navUserId = request.getHeader(NAV_USER_ID);
		if (isNotBlank(navUserId)) {
			MDC.put(MDC_USER_ID, navUserId);
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		MDC.clear();
	}
}
