package no.nav.brevserver.consumer.joark.map;

import java.util.ArrayList;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

/**
 * Base class for Dozer mappers. Incapsulates the creation of a Dozer Mapper.
 * 
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public abstract class AbstractDozerMapper {

	private static Mapper dozerMapper;

	/**
	 * Protected empty constructor.
	 */
	protected AbstractDozerMapper() {}
	
	static {
		List<String> mappingFiles = new ArrayList<String>();
		mappingFiles.add("modules/cns-joark-mapping.xml");

		dozerMapper = new DozerBeanMapper(mappingFiles);
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
