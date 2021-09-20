package config;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * Abstract database testclass. Bootstraps an in-memory H2 database.
 * Performs DDL and cleans up for each test. Also provides convenience methods for database query and updates.
 *
 * @author Joakim Bjornstad, Visma Consulting
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {H2JpaConfig.class})
@Sql(scripts = "classpath:drop-all.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(scripts = {"classpath:create-database.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class AbstractDatabaseTest {



}
