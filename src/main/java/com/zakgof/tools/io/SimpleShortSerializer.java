package com.zakgof.tools.io;

import java.io.IOException;


public class SimpleShortSerializer implements ISimpleSerializer<Short> {
  
  public static SimpleShortSerializer INSTANCE = new SimpleShortSerializer();
  
  private SimpleShortSerializer() {    
  }

  @Override
  public void write(SimpleOutputStream out, Short val) throws IOException {
    out.write(val);      
  }

  @Override
  public Short read(SimpleInputStream in) throws IOException {
    return in.readShort();
  }
  
}