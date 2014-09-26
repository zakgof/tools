package com.zakgof.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
  
  public static String rudate(LocalDate date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    String string = formatter.format(date);
    return string;    
  }
  
  public static String rudate(LocalDateTime dateTime) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    String string = formatter.format(dateTime);
    return string;    
  }

}
