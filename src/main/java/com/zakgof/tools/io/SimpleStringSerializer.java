package com.zakgof.tools.io;

import java.io.IOException;

public class SimpleStringSerializer implements ISimpleSerializer<String> {

  public static SimpleStringSerializer INSTANCE = new SimpleStringSerializer();

  private SimpleStringSerializer() {
  }

  @Override
  public void write(SimpleOutputStream out, String val) throws IOException {
    out.write(val);
  }

  @Override
  public String read(SimpleInputStream in) throws IOException {
    return in.readString();
  }

}