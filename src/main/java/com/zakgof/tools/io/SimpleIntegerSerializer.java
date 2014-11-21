package com.zakgof.tools.io;

import java.io.IOException;


public class SimpleIntegerSerializer implements ISimpleSerializer<Short> {
  
  public static SimpleIntegerSerializer INSTANCE = new SimpleIntegerSerializer();
  
  private SimpleIntegerSerializer() {    
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