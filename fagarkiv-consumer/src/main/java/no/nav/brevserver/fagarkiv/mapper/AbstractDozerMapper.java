package no.nav.brevserver.fagarkiv.mapper;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Base class for Dozer mappers. Incapsulates the creation of a Dozer Mapper.
 */
public abstract class AbstractDozerMapper {

	private static final Mapper dozerMapper;

	/**
	 * Protected empty constructor.
	 */
	protected AbstractDozerMapper() {
	}

	static {
		List<String> mappingFiles = singletonList("modules/cns-joark-mapping.xml");

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
