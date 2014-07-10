package com.zakgof.tools.io;

import java.io.IOException;


public class SimpleBooleanSerializer implements ISimpleSerializer<Boolean> {
  
  public static SimpleBooleanSerializer INSTANCE = new SimpleBooleanSerializer();
  
  private SimpleBooleanSerializer() {    
  }

  @Override
  public void write(SimpleOutputStream out, Boolean val) throws IOException {
    out.write(val ? (byte)1 : (byte)0);      
  }

  @Override
  public Boolean read(SimpleInputStream in) throws IOException {
    return in.readByte() != 0;
  }
  
}