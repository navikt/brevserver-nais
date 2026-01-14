package no.nav.brevserver.core.repository;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.properties.BrevserverProperties;
import no.nav.brevserver.core.properties.DataSourceAdditionalProperties;
import oracle.jdbc.pool.OracleDataSource;
import oracle.net.ns.SQLnetDef;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@EntityScan(basePackages = {
		"no.nav.brevserver.core.domain.entities"
})
@EnableJpaRepositories(basePackageClasses = {
		BrevtilgangRepository.class,
		BrevSystemTilgangRepository.class,
		BrevstatusRepository.class
})
@EnableTransactionManagement
@EnableConfigurationProperties({
		DataSourceProperties.class,
		DataSourceAdditionalProperties.class
})
@Configuration
@Slf4j
public class RepositoryConfig {

	@Bean
	@Primary
	DataSource dataSource(DataSourceProperties dataSourceProperties,
						  DataSourceAdditionalProperties dataSourceAdditionalProperties,
						  BrevserverProperties brevserverProperties) throws SQLException {
		PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
		poolDataSource.setConnectionFactoryClassName(OracleDataSource.class.getName());
		poolDataSource.setURL(dataSourceProperties.getUrl());
		poolDataSource.setUser(dataSourceProperties.getUsername());
		poolDataSource.setPassword(dataSourceProperties.getPassword());

		if (isOracleFastConnectionFailoverSupported(dataSourceProperties.getUrl(), dataSourceAdditionalProperties.onshosts())) {
			poolDataSource.setFastConnectionFailoverEnabled(true);
			String onsConfiguration = "nodes=" + dataSourceAdditionalProperties.onshosts();
			poolDataSource.setONSConfiguration(onsConfiguration);
			log.info("RepositoryConfig - Skrur på FCF/FAN. onsConfiguration={}", onsConfiguration);
		} else {
			// Har ikke fått system property -Doracle.jdbc.fanEnabled=false til å fungere med programmatisk oppsett av Oracle UCP.
			// Derfor er denne else blokken her
			poolDataSource.setFastConnectionFailoverEnabled(false);
			poolDataSource.setONSConfiguration("");
			log.info("RepositoryConfig - FCF/FAN er skrudd av");
		}

		Properties connProperties = new Properties();
		connProperties.setProperty(SQLnetDef.TCP_CONNTIMEOUT_STR, "3000");
		connProperties.setProperty("oracle.jdbc.thinForceDNSLoadBalancing", "true");
		// Statisk poolsize. Se brevserverProperties.java
		int poolsize = brevserverProperties.getDatabase().getPoolsize();
		log.info("Setter brevserverdb poolsize til: " + poolsize);

		poolDataSource.setInitialPoolSize(poolsize);
		poolDataSource.setMinPoolSize(poolsize);
		poolDataSource.setMaxPoolSize(poolsize);
		poolDataSource.setMaxConnectionReuseTime(300); // 5min
		poolDataSource.setMaxConnectionReuseCount(1000);
		poolDataSource.setConnectionProperties(connProperties);

		return poolDataSource;
	}

	@Bean
	@Primary
	NamedParameterJdbcTemplate namedParameterJdbcTemplate(final DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}

	private static boolean isOracleFastConnectionFailoverSupported(String jdbcurl, String onshosts) {
		return jdbcurl.toLowerCase().contains("failover") && isNotBlank(onshosts);
	}
}
