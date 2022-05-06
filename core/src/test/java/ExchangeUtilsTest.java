import no.nav.brevserver.core.utils.ExchangeUtils;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ExchangeUtilsTest {

    @Test
    public void shouldCreateGoodQueueString(){
        String expectedQueueString = "queue:///QA.P464.BISYS_REPLY_QUE?targetClient=1";

        String actualProblemQ = "queue://MPLSC02/QA.P464.BISYS_REPLY_QUE?putAsyncAllowed=1&readAheadAllowed=1";
        String actualProblemQ2 = " queue://MPLSC02/QA.P464.BISYS_REPLY_QUE?readAheadAllowed=1&putAsyncAllowed=1";

        String goodq1 = ExchangeUtils.buildReturnQueue(actualProblemQ);
        assertThat(expectedQueueString, is(goodq1));

        String goodq2 = ExchangeUtils.buildReturnQueue(actualProblemQ2);
        assertThat(expectedQueueString, is(goodq2));

    }

    @Test
    public void shouldCreateGoodQueueStringFromMinimal(){
        String badQueueString = "queue://MRP1/QA.P460.BREV_REPLY_QUE";
        String expectedQueueString = "queue:///QA.P460.BREV_REPLY_QUE?targetClient=1";
        String goodQueueString = ExchangeUtils.buildReturnQueue(badQueueString);
        assertThat(expectedQueueString, is(goodQueueString));
    }

    @Test
    public void shouldNotAddTargetClientWhenExsists(){
        String badQueueString = "queue://MPL01/QA.P464.BREV_REPLY_QUE?targetClient=1";
        String expectedQueueString = "queue:///QA.P464.BREV_REPLY_QUE?targetClient=1";
        String goodQueueString = ExchangeUtils.buildReturnQueue(badQueueString);
        assertThat(expectedQueueString, is(goodQueueString));
    }
}
