package com.KievTrung.util.helper;

import com.KievTrung.util.config.ConfigReader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FormatDateTime {
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ConfigReader.getProp("dateTime"));
  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(ConfigReader.getProp("date"));

  public static String toStr(LocalDateTime dateTime) {
	return dateTime.format(formatter);
  }

  public static String toStr(LocalDate date) {
	return date.format(dateFormatter);
  }

  public static LocalDateTime toLDT(String dateTime) {
	return LocalDateTime.parse(dateTime, formatter);
  }

  public static LocalDate toLD(String date) {
	return LocalDate.parse(date, dateFormatter);
  }
}
