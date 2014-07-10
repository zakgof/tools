package com.zakgof.tools.io;

import java.io.IOException;
import java.util.HashMap;

public class SimpleHashMapSerializer implements ISimpleSerializer<HashMap<?,?>> {

  public static SimpleHashMapSerializer INSTANCE = new SimpleHashMapSerializer();

  private SimpleHashMapSerializer() {
  }

  @Override
  public void write(SimpleOutputStream out, HashMap<?, ?> val) throws IOException {
    
  }

  @Override
  public HashMap<?, ?> read(SimpleInputStream in) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  

}