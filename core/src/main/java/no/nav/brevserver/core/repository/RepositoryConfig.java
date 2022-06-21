package no.nav.brevserver.core.repository;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.alias.BrevserverProperties;
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
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

@EntityScan(basePackages = {
		"no.nav.brevserver.core.domain.entities"
})
@EnableJpaRepositories(basePackageClasses = {
		BrevtilgangRepository.class,
		BrevSystemTilgangRepository.class,
		BrevstatusRepository.class
})
@EnableTransactionManagement
@EnableConfigurationProperties({DataSourceProperties.class, BrevserverProperties.class})
@Configuration
@Slf4j
public class RepositoryConfig {

	@Bean
	@Primary
	DataSource dataSource(final DataSourceProperties dataSourceProperties) throws SQLException {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
		dataSource.setUrl(dataSourceProperties.getUrl());
		dataSource.setUsername(dataSourceProperties.getUsername());
		dataSource.setPassword(dataSourceProperties.getPassword());
		return dataSource;
	}

	@Bean
	@Primary
	NamedParameterJdbcTemplate namedParameterJdbcTemplate(final DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}
}
