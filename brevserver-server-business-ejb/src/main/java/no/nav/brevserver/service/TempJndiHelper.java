package no.nav.brevserver.service;

import no.nav.brevserver.server.common.config.ConfigManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public class TempJndiHelper {

	public static DataSource jndiDataSource() {
		String username = ConfigManager.getInstance().getString(ConfigManager.DATABASE_USERNAME, null);
		String password = ConfigManager.getInstance().getString(ConfigManager.DATABASE_PASSWORD, null);
		String url = ConfigManager.getInstance().getString(ConfigManager.DATABASE_URL, null);
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.ibm.db2.jcc.DB2Driver");
		dataSource.setUrl(url);
		dataSource.setSchema("BS475Q");
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		return dataSource;
	}
}
