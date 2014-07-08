package com.zakgof.tools;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;

public class Buffer implements Serializable {
  
  private static final long serialVersionUID = 1082630234L;
  
  public Buffer(byte[] bytes) {
    this.bytes = bytes;
  }
  private final byte[] bytes;
  
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
}