package com.zakgof.tools.io;

import java.io.IOException;


public class SimpleByteSerializer implements ISimpleSerializer<Byte> {
  
  public static SimpleByteSerializer INSTANCE = new SimpleByteSerializer();
  
  private SimpleByteSerializer() {    
  }

  @Override
  public void write(SimpleOutputStream out, Byte val) throws IOException {
    out.write(val);      
  }

  @Override
  public Byte read(SimpleInputStream in) throws IOException {
    return in.readByte();
  }
  
}