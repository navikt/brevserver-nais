package no.nav.brevserver.ws.loggmottak.map.support;

import no.nav.brevserver.service.loggmottak.to.LoggRequest;
import no.nav.brevserver.ws.loggmottak.map.LoggRequestMapper;
import org.springframework.stereotype.Component;
import no.nav.brevserver.nais.support.AbstractProviderDozerMapper;

@Component
public class DefaultLoggRequestMapper extends AbstractProviderDozerMapper implements LoggRequestMapper {

	@Override
	public LoggRequest map(no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest loggRequest) {
		return getDozerMapper().map(loggRequest, LoggRequest.class);
	}
}
