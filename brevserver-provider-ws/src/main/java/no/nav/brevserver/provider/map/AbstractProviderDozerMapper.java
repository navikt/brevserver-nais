package no.nav.brevserver.provider.map;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

import java.util.Arrays;

/**
 * Base class for Dozer mappers. Incapsulates the creation of a Dozer Mapper.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public abstract class AbstractProviderDozerMapper {

	private static Mapper dozerMapper;

	/**
	 * Protected empty constructor.
	 */
	protected AbstractProviderDozerMapper() {
	}

	static {
		dozerMapper = new DozerBeanMapper(
				Arrays.asList(
						"dozer/provider-dokumentbehandling-mapping.xml",
						"dozer/provider-loggmottak-mapping.xml"
				)
		);
	}

	/**
	 * Getter for the dozerMapper property.
	 *
	 * @return the dozerMapper
	 */
	protected static Mapper getDozerMapper() {
		return dozerMapper;
	}

}
