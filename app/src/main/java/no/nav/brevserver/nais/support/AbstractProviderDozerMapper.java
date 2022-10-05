package no.nav.brevserver.nais.support;


import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;

import java.util.Arrays;

/**
 * Base class for Dozer mappers. Incapsulates the creation of a Dozer Mapper.
 */
public abstract class AbstractProviderDozerMapper {

	private static final Mapper dozerMapper;

	/**
	 * Protected empty constructor.
	 */
	protected AbstractProviderDozerMapper() {
	}

	static {
		var mappingFiles = Arrays.asList(
				"dozer/provider-dokumentbehandling-mapping.xml",
				"dozer/provider-loggmottak-mapping.xml"
		);
		dozerMapper = DozerBeanMapperBuilder.create()
				.withMappingFiles(mappingFiles)
				.build();
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
