package com.zakgof.tools;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;

import com.google.common.primitives.UnsignedBytes;

public class Buffer implements Comparable<Buffer>, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -7890415542517043991L;

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
    if (!(o instanceof Buffer))
      return false;
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

  @Override
  public int compareTo(Buffer that) {
    return UnsignedBytes.lexicographicalComparator().compare(this.bytes, that.bytes);
  }
}