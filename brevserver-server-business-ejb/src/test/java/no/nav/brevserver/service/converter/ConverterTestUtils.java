package no.nav.brevserver.service.converter;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import no.nav.brevserver.server.common.log.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ConverterTestUtils {

	/**
	 * Generates a byte array representation of the contents of a simple RTF-file. 
	 * @return The byte[] of the rtf file
	 */
	public static byte[] getSimpleRtf() {
		return "{\\rtf1 Dette er en test}".getBytes();
	}

	public static byte[] getRealRft(Path pathToRtf) throws IOException {
		return Files.readAllBytes(pathToRtf);
	}

	public static void saveByteArrayToDisk(byte[] blob, Path filePath) throws IOException {
		Files.write(filePath, blob, StandardOpenOption.CREATE);
	}

	/**
	 * Mocks the constructor of the {@link Log} class
	 * 
	 * @return The mock representing the {@link Log} class
	 * @throws Exception
	 */
	public Log mockLog() throws Exception {
		mockStatic(Log.class);
		Log logMock = mock(Log.class);
		whenNew(Log.class).withParameterTypes(Class.class).withArguments(isA(Class.class)).thenReturn(logMock);
		return logMock;
	}
}
