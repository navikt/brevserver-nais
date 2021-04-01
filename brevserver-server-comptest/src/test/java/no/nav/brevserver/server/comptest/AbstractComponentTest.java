package no.nav.brevserver.server.comptest;

import no.nav.brevserver.server.comptest.util.MessageHandler;
import no.nav.brevserver.service.brevlager.BrevlagerService;
import no.nav.brevserver.service.brevlager.BrevlagerServiceFactory;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:brevserver-remote-context.xml")
public class AbstractComponentTest {

	protected String brevreferanse = "123456789";

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	@Autowired
	protected JmsTemplate jmsTemplate;

	@Autowired
	protected MessageHandler messageHandler;

	protected BrevlagerService brevlager = BrevlagerServiceFactory.getInstance().createBrevlagerService();
	protected BrevserverService brevserver = BrevserverServiceFactory.getInstance().createBrevserverService();

	@After
	public void after() {
		cleanDatabase(brevreferanse);
	}

	@Before
	public void before() throws Exception {

	}

	@Test
	@Ignore
	public void TestfileMustIncludeOneTest() throws Exception {
	}

	protected void cleanDatabase(String... brevreferanser) {
		StringBuilder builder = new StringBuilder("(");
		for (String brevreferanse : brevreferanser) {
			builder.append("'").append(brevreferanse).append("'").append(",");
		}
		builder.deleteCharAt(builder.length() - 1).append(")");
		jdbcTemplate.execute("delete from T_BREVLAGER_X where BREVREFERANSE in " + builder.toString());
		jdbcTemplate.execute("delete from T_BREVSTATUS where BREVREFERANSE in " + builder.toString());
		jdbcTemplate.execute("delete from T_BREVTILGANG where BREVREFERANSE in " + builder.toString());
		jdbcTemplate.execute("delete from T_BREVLAGER_HISTORIKK where BREVREFERANSE in " + builder.toString());
	}

	protected void cleanQueues(String... queues) {
		for (String queue : queues) {
			messageHandler.consumeMessages(queue);
		}
	}

}
