package com.KievTrung.util.db;

import com.KievTrung.util.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class DBInitializer {
  public static void init() throws SQLException {
	try (Connection con = Database.getConnection();
		 Statement statement = con.createStatement();
		 FileReader reader = new FileReader(ConfigReader.getProp("schemaPath"))) {
	  String[] sqls = reader.readAllAsString().split(";");
	  for (String s : sqls) {
		statement.execute(s);
		log.info("Executed sql\n{}", s.strip());
	  }
	} catch (IOException e) {
	  log.error("Schema file not found");
	  throw new RuntimeException(e);
	}
  }
}
