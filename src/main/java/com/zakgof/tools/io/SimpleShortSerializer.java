package com.zakgof.tools.io;

import java.io.IOException;


public class SimpleShortSerializer implements ISimpleSerializer<Integer> {
  
  public static SimpleShortSerializer INSTANCE = new SimpleShortSerializer();
  
  private SimpleShortSerializer() {    
  }

  @Override
  public void write(SimpleOutputStream out, Integer val) throws IOException {
    out.write(val);      
  }

  @Override
  public Integer read(SimpleInputStream in) throws IOException {
    return in.readInt();
  }
  
}