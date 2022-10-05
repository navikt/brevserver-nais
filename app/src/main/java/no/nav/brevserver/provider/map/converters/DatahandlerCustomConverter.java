package no.nav.brevserver.provider.map.converters;

import com.github.dozermapper.core.DozerConverter;
import org.apache.commons.io.IOUtils;

import javax.activation.DataHandler;
import java.io.IOException;

/**
 * Converts between DataHandler and byte[]
 */
public class DatahandlerCustomConverter extends DozerConverter<DataHandler, byte[]> {
	public DatahandlerCustomConverter() {
		super(DataHandler.class, byte[].class);
	}

	@Override
	public byte[] convertTo(DataHandler source, byte[] destination) {
		if (source == null) {
			return null;
		}

		try {
			return IOUtils.toByteArray(source.getInputStream());
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public DataHandler convertFrom(byte[] source, DataHandler destination) {
		throw new UnsupportedOperationException("Convert byte[] to DataHandler is not supported");
	}
}
