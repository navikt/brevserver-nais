import no.nav.brevserver.core.utils.ExchangeUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExchangeUtilsTest {

    @Test
    public void shouldCreateGoodQueueString(){
        String expectedQueueStringWithAsynch = "queue:///QA.P464.BISYS_REPLY_QUE?putAsyncAllowed=1&targetClient=1";
        String expectedQueueStringWithOutAsynch = "queue:///QA.P464.BISYS_REPLY_QUE?targetClient=1";

        String badQueueStringAnd = "queue://MPLSC01/QA.P464.BISYS_REPLY_QUE?putAsyncAllowed=1&readAheadAllowed=1";
        String goodQueueStringAnd = ExchangeUtils.buildReturnQueue(badQueueStringAnd);
        assertThat(expectedQueueStringWithAsynch, is(goodQueueStringAnd));


        String badQueueStringQuestion = "queue://MPLSC01/QA.P464.BISYS_REPLY_QUE?readAheadAllowed=1&putAsyncAllowed=1";
        String goodQueueStringQuestion = ExchangeUtils.buildReturnQueue(badQueueStringQuestion);
        assertThat(expectedQueueStringWithAsynch, is(goodQueueStringQuestion));

        String badQueueStringOnlyBadParameter = "queue://MPLSC01/QA.P464.BISYS_REPLY_QUE?readAheadAllowed=1";
        String goodQueueStringOnlyBadParameter = ExchangeUtils.buildReturnQueue(badQueueStringOnlyBadParameter);
        assertThat(expectedQueueStringWithOutAsynch, is(goodQueueStringOnlyBadParameter));
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
