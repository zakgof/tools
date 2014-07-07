package com.zakgof.tools.io;

import java.io.IOException;


public class SimpleIntegerSerializer implements ISimpleSerializer<Integer> {
  
  public static SimpleIntegerSerializer INSTANCE = new SimpleIntegerSerializer();
  
  private SimpleIntegerSerializer() {    
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