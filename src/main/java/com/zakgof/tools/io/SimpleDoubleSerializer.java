package com.zakgof.tools.io;

import java.io.IOException;


public class SimpleDoubleSerializer implements ISimpleSerializer<Double> {
  
  public static SimpleDoubleSerializer INSTANCE = new SimpleDoubleSerializer();
  
  private SimpleDoubleSerializer() {    
  }

  @Override
  public void write(SimpleOutputStream out, Double val) throws IOException {
    out.write(val);      
  }

  @Override
  public Double read(SimpleInputStream in) throws IOException {
    return in.readDouble();
  }
  
}