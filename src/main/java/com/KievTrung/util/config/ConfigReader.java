package com.KievTrung.util.config;

import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

@Slf4j
public class ConfigReader {
  private static final Properties props = new Properties();

  static {
	try (Reader reader = new FileReader("src/main/resources/app.properties")) {
	  props.load(reader);
	} catch (IOException e) {
	  log.error("Properties file not found");
	}
  }

  public static String getProp(String key) {
	return props.getProperty(key);
  }
}
