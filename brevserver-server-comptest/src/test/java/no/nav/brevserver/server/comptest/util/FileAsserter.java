package no.nav.brevserver.server.comptest.util;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class FileAsserter {

	private static final String pdf = "%PDF";
	private static final String pdf_error = "File is not of type PDF.";
	
	public static void assertThatFileIsPDF(File file) {
		String line = "";
		try {
			line = (String) FileUtils.lineIterator(file, null).next();
		} catch (IOException e) {
		}
		assertThatFileIsPDF(line);
	}
	
	public static void assertThatFileIsPDF(byte[] file) {
		String fileContent = new String(file);
		assertThatFileIsPDF(fileContent);
	}
	
	public static void assertThatFileIsPDF(String fileContent) {
		if (fileContent.length() < pdf.length()) {
			fail(pdf_error);
		}
		if (!pdf.equals(StringUtils.substring(fileContent, 0, 4))) {
			fail(pdf_error);
		}
	}
	
}
