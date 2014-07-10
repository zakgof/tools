package com.zakgof.tools.io;

import java.io.IOException;

public class SimpleByteArraySerializer implements ISimpleSerializer<byte[]> {

  public static SimpleByteArraySerializer INSTANCE = new SimpleByteArraySerializer();

  private SimpleByteArraySerializer() {
  }

  @Override
  public void write(SimpleOutputStream out, byte[] val) throws IOException {
    out.write(val);
  }

  @Override
  public byte[] read(SimpleInputStream in) throws IOException {
    return in.readBytes();
  }

}