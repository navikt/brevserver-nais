package no.nav.brevserver.provider.map.support;

import no.nav.brevserver.provider.map.AbstractProviderDozerMapper;
import no.nav.brevserver.provider.map.LoggRequestMapper;
import no.nav.brevserver.service.loggmottak.to.LoggRequest;

/**
 * Default implementation of LoggRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultLoggRequestMapper extends AbstractProviderDozerMapper implements LoggRequestMapper {

	@Override
	public LoggRequest map(no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest loggRequest) {
		return getDozerMapper().map(loggRequest, LoggRequest.class);
	}
}
