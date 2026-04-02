package com.KievTrung.util.db;

import com.KievTrung.util.config.ConfigReader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {
  private static final HikariDataSource dataSource = createDataSource();

  private static HikariDataSource createDataSource() {
	HikariConfig config = new HikariConfig();
	config.setJdbcUrl(ConfigReader.getProp("jdbcUrl"));
	config.setDriverClassName(ConfigReader.getProp("driverClassName"));
	return new HikariDataSource(config);
  }

  public static Connection getConnection() throws SQLException {
	return dataSource.getConnection();
  }
}
