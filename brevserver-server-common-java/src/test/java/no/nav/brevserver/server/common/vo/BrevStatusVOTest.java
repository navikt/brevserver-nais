package no.nav.brevserver.server.common.vo;

import no.nav.brevserver.server.common.config.Konstanter;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link BrevStatusVO}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 * @author Thomas Kasene, Visma Consulting AS
 */
public class BrevStatusVOTest {

	private static final String TOKEN = "1913883917901";

	@Test
	public void shouldCensorToken() throws Exception {
		BrevStatusVO messageVO = new BrevStatusVO();
		messageVO.setToken(TOKEN);

		assertThat(messageVO.getCensoredToken(), equalTo(Konstanter.MASKED_PASSWORD + "901"));
	}

	@Test
	public void shouldHandleNullTokenWhenCensoring() throws Exception {
		BrevStatusVO messageVO = new BrevStatusVO();
		messageVO.setToken(null);

		assertThat(messageVO.getCensoredToken(), equalTo(Konstanter.MASKED_PASSWORD));
	}
}
