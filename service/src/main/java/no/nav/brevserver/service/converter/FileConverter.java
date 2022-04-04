package no.nav.brevserver.service.converter;

import com.aspose.words.Document;
import com.aspose.words.FontSettings;
import com.aspose.words.License;
import com.aspose.words.SaveFormat;
import no.nav.brevserver.service.config.ConfigManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Converts documents to different types using the aspose library.
 * 
 * @author Stian Landsnes, Visma Sirius
 * @author Marius Thøring, Visma Consulting
 * 
 */
public final class FileConverter {

	private static boolean useLicense = true;
	private static boolean useFontsFolder = true;

	private static FileConverter converter;

	/**
	 * Constructor should not be visible.
	 */
	private FileConverter(boolean useFontsFolder, boolean useLicense) {
		if (useFontsFolder) {
			setUpFonts();
		}
		if (useLicense) {
			setUpLicense();
		}
	}

	/**
	 * Converts document to PDF.
	 * 
	 * @param documentData
	 *            The byte[] containing the document to convert to PDF.
	 * @return A document in PDF format.
	 * @throws Exception
	 *             If error in conversion.
	 */
	public byte[] convertToPdf(byte[] documentData) throws Exception {
		Document document = new Document(new ByteArrayInputStream(documentData));
		ByteArrayOutputStream convertedData = new ByteArrayOutputStream();
		document.save(convertedData, SaveFormat.PDF);
		return convertedData.toByteArray();
	}

	private void setUpFonts() {
		String fontsLocation = ConfigManager.getInstance().getString(ConfigManager.ASPOSE_FONTS_LOCATION, null);
		if (StringUtils.isEmpty(fontsLocation)) {
			throw new RuntimeException(
					"Feil ved oppsett av fonter for Aspose! Sti til fontmappe ble ikke funnet i konfigurasjonen");
		}
		try {
			URL folder = getClass().getClassLoader().getResource(fontsLocation);
			FontSettings.setFontsFolder(folder.getPath(), true);
		} catch (Exception e) {
			throw new RuntimeException("Feil ved oppsett av fonter for Aspose! "
					+ "Sjekk at mappen som er angitt i konfigurasjonen eksisterer: " + fontsLocation, e);
		}
	}

	private void setUpLicense() {
		String licenseLocation = ConfigManager.getInstance().getString(ConfigManager.ASPOSE_LICENSE_LOCATION, null);
		if (StringUtils.isEmpty(licenseLocation)) {
			throw new RuntimeException(
					"Feil ved lasting av lisens for Aspose! Sti til lisensfil ble ikke funnet i konfigurasjonen");
		}
		try {
			new License().setLicense(new ClassPathResource(licenseLocation).getInputStream());
		} catch (IOException e) {
			throw new RuntimeException("Feil ved lasting av lisens for Aspose! "
					+ "Sjekk at filen som er angitt i konfigurasjonen eksisterer: " + licenseLocation, e);
		} catch (Exception e) {
			throw new RuntimeException("Feil ved lasting av lisens for Aspose! "
					+ "Sjekk at stien i konfigurasjonen peker på riktig fil og at lisensen ikke er utgått på dato", e);
		}
	}

	/**
	 * Gets a static instance of {@link FileConverter}.
	 * 
	 * @return The static instance of {@link FileConverter}.
	 */
	public static FileConverter getInstance() {
		if (converter == null) {
			converter = new FileConverter(useFontsFolder, useLicense);
		}
		return converter;
	}

	/**
	 * Sets whether or not to use the fonts folder supplied in the configuration file. Default is true.
	 */
	public static void setUseFontsFolder(boolean useFontsFolder) {
		FileConverter.useFontsFolder = useFontsFolder;
		converter = null;
	}

	/**
	 * Sets whether or not to use the license file supplied in the configuration file. Default is true.
	 */
	public static void setUseLicense(boolean useLicense) {
		FileConverter.useLicense = useLicense;
		converter = null;
	}
}
