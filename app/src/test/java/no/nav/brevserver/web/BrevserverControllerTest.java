package no.nav.brevserver.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class BrevserverControllerTest {

	private final String expected = "brevserver";

	@Test
	public void testValidationOkPE() {
		Model modelMock = mock(Model.class);
		BrevserverController brevserverController = new BrevserverController();
		String result = brevserverController.startBrevklient("PE2", "123456789", "TOKEN", "60", "60", modelMock);
		assertEquals(result, expected);
	}

	@Test
	public void testValidationOkBI() {
		Model modelMock = mock(Model.class);
		BrevserverController brevserverController = new BrevserverController();
		String result = brevserverController.startBrevklient("BI12", "123456789", "TOKEN", "60", "60", modelMock);
		assertEquals(result, expected);
	}

	@Test
	public void testValidationFailBrevreferanse() {
		Model modelMock = mock(Model.class);
		BrevserverController brevserverController = new BrevserverController();
		RuntimeException thrown = Assertions
				.assertThrows(RuntimeException.class, () -> {
					brevserverController.startBrevklient("BI12", "12345X6789", "TOKEN", "60", "60", modelMock);
				}, "");

		Assertions.assertEquals("Brevreferanse er ikke gyldig", thrown.getMessage());
	}

	@Test
	public void testValidationFailSystemId() {
		Model modelMock = mock(Model.class);
		BrevserverController brevserverController = new BrevserverController();
		RuntimeException thrown = Assertions
				.assertThrows(RuntimeException.class, () -> {
					brevserverController.startBrevklient("BII2", "123456789", "TOKEN", "60", "60", modelMock);
				}, "");

		Assertions.assertEquals("Systemid er ikke gyldig", thrown.getMessage());
	}
}
