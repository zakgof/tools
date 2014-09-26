package com.zakgof.tools.io;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;

public class SimpleLocalDateTimeSerializer implements ISimpleSerializer<LocalDateTime> {

  public static SimpleLocalDateTimeSerializer INSTANCE = new SimpleLocalDateTimeSerializer();

  private SimpleLocalDateTimeSerializer() {
  }

  @Override
  public void write(SimpleOutputStream out, LocalDateTime val) throws IOException {
    long epochDay = val.getLong(ChronoField.EPOCH_DAY);
    int dayMilli = val.get(ChronoField.MILLI_OF_DAY);
    out.write(epochDay);
    out.write(dayMilli);
  }

  @Override
  public LocalDateTime read(SimpleInputStream in) throws IOException {
    Long epochDay = in.readLong();
    Integer dayMilli = in.readInt();
    if (epochDay == null || dayMilli == null)
      return null;
    return LocalDateTime.of(LocalDate.ofEpochDay(epochDay), LocalTime.ofNanoOfDay(1000L * dayMilli));
  }

}