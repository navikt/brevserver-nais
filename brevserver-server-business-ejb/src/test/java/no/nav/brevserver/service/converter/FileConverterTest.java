package no.nav.brevserver.service.converter;

import no.nav.brevserver.server.common.config.ConfigManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigManager.class})
public class FileConverterTest {

	@Before
	public void setUp() throws Exception {
		mockConfigManager();
		FileConverter.setUseFontsFolder(true);
		FileConverter.setUseLicense(true);
	}

	@Test
	public void shouldCreatePdfFromRtf() throws Exception {
		byte[] output = FileConverter.getInstance().convertToPdf(ConverterTestUtils.getRealRft(new ClassPathResource("aspose/rtf/RTF.rtf").getFile().toPath()));

		assertFalse("Generert PDF er null", output == null);
		assertTrue("Generert PDF har størrelse 0", output.length > 0);
		Path outputPath = new File("target", "RTF.pdf").toPath();
		ConverterTestUtils.saveByteArrayToDisk(output, outputPath);
	}
	
	private void mockConfigManager() {
		ConfigManager configManagerMock = mock(ConfigManager.class);
		mockStatic(ConfigManager.class);
		when(ConfigManager.getInstance()).thenReturn(configManagerMock);
		when(configManagerMock.getString(ConfigManager.ASPOSE_FONTS_LOCATION, null)).thenReturn("aspose/fonts");
		when(configManagerMock.getString(ConfigManager.ASPOSE_LICENSE_LOCATION, null)).thenReturn("aspose/license/Aspose.Words.lic");
	}
}
