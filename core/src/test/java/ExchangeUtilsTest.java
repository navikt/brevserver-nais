import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static no.nav.brevserver.core.utils.ExchangeUtils.buildReturnQueue;
import static org.assertj.core.api.Assertions.assertThat;

public class ExchangeUtilsTest {

	@ParameterizedTest
	@ValueSource(strings = {
			"queue://MPLSC02/QA.P464.BISYS_REPLY_QUE?putAsyncAllowed=1&readAheadAllowed=1",
			"queue://MPLSC02/QA.P464.BISYS_REPLY_QUE?readAheadAllowed=1&putAsyncAllowed=1",
			"queue://MPLSC02/QA.P464.BISYS_REPLY_QUE?putAsyncAllowed=1&targetClient=1"
	})
	public void shouldBuildReturnQueue(String queue) {
		String expectedQueue = "queue:///QA.P464.BISYS_REPLY_QUE?targetClient=1";

		String returnQueue = buildReturnQueue(queue);

		assertThat(returnQueue).isEqualTo(expectedQueue);
	}

	@Test
	public void shouldBuildReturnQueueFromMinimal() {
		String minimalQueue = "queue://MRP1/QA.P460.BREV_REPLY_QUE";
		String expectedQueue = "queue:///QA.P460.BREV_REPLY_QUE?targetClient=1";

		String returnQueue = buildReturnQueue(minimalQueue);

		assertThat(returnQueue).isEqualTo(expectedQueue);
	}

	@Test
	public void shouldNotAddTargetClientWhenExists() {
		String queue = "queue://MPL01/QA.P464.BREV_REPLY_QUE?targetClient=1";
		String expectedQueue = "queue:///QA.P464.BREV_REPLY_QUE?targetClient=1";

		String returnQueue = buildReturnQueue(queue);

		assertThat(returnQueue).isEqualTo(expectedQueue);
	}

}