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
  
  private static final String RU_SHORT_WEEKDAYS[] = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
  
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
  
  public static String weight(float value) {
    if (value == 0)
      return "?";
    if (Math.abs(value - Math.floor(value)) < 0.01)
      return String.format("%.0f", Math.floor(value));
    return String.format("%.1f", value);    
  }

  public static Float parseFloat(String s) {
    if (s == null)
      return null;
    try {
      return Float.parseFloat(s);      
    } catch (NumberFormatException e) {      
    }
    try {
      return Float.parseFloat(s.replace(',', '.'));      
    } catch (NumberFormatException e) {      
    }
    return null;        
  }

  public static LocalDate parseDate(String date) {
    try {
      return LocalDate.parse(date, DateTimeFormatter.ofPattern("d.MM.yyyy"));
    } catch (Exception e) {
      return null;
    }
  }

}
