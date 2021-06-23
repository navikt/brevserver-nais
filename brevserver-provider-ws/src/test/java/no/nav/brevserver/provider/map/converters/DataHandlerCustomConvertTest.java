package no.nav.brevserver.provider.map.converters;

import com.sun.istack.ByteArrayDataSource;
import no.nav.brevserver.server.common.vo.FilType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.activation.DataHandler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for DatahandlerCustomConverter
 *
 * @author Joakim Bj�rnstad, Visma Consulting
 */
public class DataHandlerCustomConvertTest {

	private static final byte[] DOKUMENTDATA = "Dokument".getBytes();

	private DatahandlerCustomConverter datahandlerCustomConverter;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		datahandlerCustomConverter = new DatahandlerCustomConverter();
	}

	@Test
	public void shouldConvertDataHandlerToByteArray() {
		DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(DOKUMENTDATA, FilType.RTF.getContentType()));

		byte[] actualByteArray = datahandlerCustomConverter.convertTo(dataHandler, null);

		assertThat(actualByteArray, is(DOKUMENTDATA));
	}

	@Test
	public void shouldThrowExceptionIfConvertFromIsCalled() {
		thrown.expect(UnsupportedOperationException.class);
		thrown.expectMessage("Convert byte[] to DataHandler is not supported");

		datahandlerCustomConverter.convertFrom(DOKUMENTDATA, null);
	}
}
