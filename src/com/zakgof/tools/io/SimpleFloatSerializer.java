package com.zakgof.tools.io;

import java.io.IOException;


public class SimpleFloatSerializer implements ISimpleSerializer<Float> {
  
  public static SimpleFloatSerializer INSTANCE = new SimpleFloatSerializer();
  
  private SimpleFloatSerializer() {    
  }

  @Override
  public void write(SimpleOutputStream out, Float val) throws IOException {
    out.write(val);      
  }

  @Override
  public Float read(SimpleInputStream in) throws IOException {
    return in.readFloat();
  }
  
}