package no.nav.brevserver.command;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import no.nav.brevserver.server.common.config.Konstanter;
import org.junit.Test;

/**
 * Unit tests for {@link XmlLogger}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class XmlLoggerTest {

	private static final String ACTUAL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<rtv-brev klientToken=\"892389238923\" modus=\"frabrevlager\" passord=\"1923123\" saksbehandler=\"B192919\" sysid=\"PE2\"><brev brevref=\"36728820\"/></rtv-brev>";

	private static final String EXPECTED = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<rtv-brev klientToken=\"" + Konstanter.MASKED_PASSWORD + "\" modus=\"frabrevlager\" passord=\"" + Konstanter.MASKED_PASSWORD
			+ "\" saksbehandler=\"B192919\" sysid=\"PE2\"><brev brevref=\"36728820\"/></rtv-brev>";

	@Test
	public void shouldRemoveCleartextPasswords() throws Exception {
		String result = XmlLogger.removePasswords(ACTUAL);

		assertThat(result, equalTo(EXPECTED));
	}

	@Test
	public void shouldHandleNull() throws Exception {
		assertThat(XmlLogger.removePasswords(null), nullValue());
	}

}