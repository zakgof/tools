package com.zakgof.tools;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

public class Buffer {

  @SuppressWarnings("unused")
  @Deprecated
  private Buffer() {
  }
  
  public Buffer(byte[] bytes) {
    this.bytes = bytes;
  }
  
  private byte[] bytes;
  
  @Override
  public boolean equals(Object o) {
    return Arrays.equals(bytes, ((Buffer)o).bytes);
  }
  
  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }
  
  public InputStream stream() {
    return new ByteArrayInputStream(bytes);
  }

  public byte[] bytes() {
    return bytes;
  }

  public int size() {
    return bytes.length;
  }
}