package com.zakgof.tools.io;

import java.io.IOException;


public class SimpleLongSerializer implements ISimpleSerializer<Long> {
  
  public static SimpleLongSerializer INSTANCE = new SimpleLongSerializer();
  
  private SimpleLongSerializer() {    
  }

  @Override
  public void write(SimpleOutputStream out, Long val) throws IOException {
    out.write(val);      
  }

  @Override
  public Long read(SimpleInputStream in) throws IOException {
    return in.readLong();
  }
  
}