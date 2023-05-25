package no.nav.brevserver.web;

import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class BrevserverLegacyControllerTest {

	private final String expected = "brevserver";

	@Test
	public void testValidationOkPE() {
		Model modelMock = mock(Model.class);
		BrevserverLegacyController brevserverLegacyController = new BrevserverLegacyController("https://itest");
		String result = brevserverLegacyController.startBrevklient("PE2", "123456789", "TOKEN", "60", "60", modelMock);
		assertEquals(result, expected);
	}

	@Test
	public void testValidationOkBI() {
		Model modelMock = mock(Model.class);
		BrevserverLegacyController brevserverLegacyController = new BrevserverLegacyController("https://itest");
		String result = brevserverLegacyController.startBrevklient("BI12", "123456789", "TOKEN", "60", "60", modelMock);
		assertEquals(result, expected);
	}
}
