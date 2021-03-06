package com.zakgof.tools.io;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoField;

public class SimpleLocalDateSerializer implements ISimpleSerializer<LocalDate> {

  public static SimpleLocalDateSerializer INSTANCE = new SimpleLocalDateSerializer();

  private SimpleLocalDateSerializer() {
  }

  @Override
  public void write(SimpleOutputStream out, LocalDate val) throws IOException {
    long epochDay = val.getLong(ChronoField.EPOCH_DAY);
    out.write(epochDay);
  }

  @Override
  public LocalDate read(SimpleInputStream in) throws IOException {
    Long epochDay = in.readLong();
    if (epochDay == null)
      return null;
    return LocalDate.ofEpochDay(epochDay);
  }

}