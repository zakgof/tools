package com.zakgof.tools.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class SimpleInputStream implements AutoCloseable {

  private final InputStream stream;
  private final long position = 0;

  public SimpleInputStream(InputStream stream) {
    this.stream = stream;
  }

  public String readString() throws IOException {
    Integer length = readInt();
    if (length == null || (int)length < 0 || length > 1024 * 1024 * 1024)
      return null;
    byte[] buffer = new byte[length];
    int bytes = read(buffer);
    if (bytes != length)
      throw new IOException("Can't read string");
    return new String(buffer, "cp1251"); // TODO
  }

  public byte[] readBytes() throws IOException {
    Integer length = readInt();
    if (length == null || length < 0 || length > 1024 * 1024 * 1024)
      return null;
    byte[] buffer = new byte[length];
    int bytes = read(buffer);
    if (bytes != length)
      throw new IOException("Can't read bytes");
    return buffer;
  }

  public byte readByte() throws IOException {
    return (byte) (stream.read() & 0xFF);
  }
  
  public Short readShort() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(2);
    int bytes = read(buffer.array());
    if (bytes == -1)
      return null;
    else if (bytes == 2)
      return buffer.asShortBuffer().get();
    throw new IOException("Can't read short");
  }

  public Integer readInt() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    int bytes = read(buffer.array());
    if (bytes == -1)
      return null;
    else if (bytes == 4)
      return buffer.asIntBuffer().get();
    throw new IOException("Can't read integer");
  }

  public Long readLong() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(8);
    int bytes = read(buffer.array());
    if (bytes == -1)
      return null;
    else if (bytes == 8)
      return buffer.asLongBuffer().get();
    throw new IOException("Can't read long");
  }

  public Double readDouble() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(8);
    int bytes = read(buffer.array());
    if (bytes == -1)
      return null;
    else if (bytes == 8)
      return buffer.asDoubleBuffer().get();
    throw new IOException("Can't read double");
  }

  public Float readFloat() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(6);
    int bytes = read(buffer.array());
    if (bytes == -1)
      return null;
    else if (bytes == 6)
      return buffer.asFloatBuffer().get();
    throw new IOException("Can't read float");
  }

  public void close() {
    try {
      stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public long getPosition() {
    return position;
  }
  
  private int read(byte[] buffer) throws IOException {
    int offset = 0;
    while(offset < buffer.length) {
      int chunk = stream.read(buffer, offset, buffer.length - offset);
      if (chunk < 0)
        throw new IOException("Read error - stream end");
      offset += chunk;
    }
    return offset;
  }

}
