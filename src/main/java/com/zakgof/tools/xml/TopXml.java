package com.zakgof.tools.xml;

public class TopXml extends Xml {
  @Override
  public String toString() {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + super.toString();
  }
}