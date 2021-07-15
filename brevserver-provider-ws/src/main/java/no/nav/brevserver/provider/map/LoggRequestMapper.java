package no.nav.brevserver.provider.map;

import no.nav.brevserver.service.loggmottak.to.LoggRequest;

/**
 * Interface for mapping between webservice and domain logg request
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface LoggRequestMapper {
	/**
	 * Maps from webservice request to domain request for logg
	 *
	 * @param loggRequest The webservice request
	 * @return The mapped domain request
	 */
	LoggRequest map(no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest loggRequest);
}
