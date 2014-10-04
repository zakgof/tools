package com.zakgof.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

public class Utils {
  
  public static String rudate(LocalDate date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    String string = formatter.format(date);
    return string;    
  }
  
  public static String rutime(LocalTime time) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    String string = formatter.format(time);
    return string;    
  }
  
  private static final String RU_SHORT_WEEKDAYS[] = {"Ïí", "Âò", "Ñð", "×ò", "Ïò", "Ñá", "Âñ"};
  
  public static String rushortweek(LocalDate date) {
    int d = date.get(ChronoField.DAY_OF_WEEK) - 1;
    return RU_SHORT_WEEKDAYS[d];
  }
  
  public static String rudate(LocalDateTime dateTime) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    String string = formatter.format(dateTime);
    return string;    
  }
  
  public static String shorten(String orig, int limit) {
    if (orig.length() < limit)
      return orig;
    return orig.substring(0, limit-3) + "...";
  }
  
  public static String fixfloat(float value, int digits) {
//    if (value == null)
//      return "?";
    return String.format(String.format("%%.%df", digits), value);
  }

}
