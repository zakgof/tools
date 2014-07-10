package com.zakgof.tools.io;

import java.io.IOException;
import java.util.Date;

public class SimpleDateSerializer implements ISimpleSerializer<Date> {

  public static SimpleDateSerializer INSTANCE = new SimpleDateSerializer();

  private SimpleDateSerializer() {
  }

  @Override
  public void write(SimpleOutputStream out, Date val) throws IOException {
    out.write(val.getTime());
  }

  @Override
  public Date read(SimpleInputStream in) throws IOException {
    Long ms = in.readLong();
    if (ms == null)
      return null;
    return new Date(ms);
  }

}