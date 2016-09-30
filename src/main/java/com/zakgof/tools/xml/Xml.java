package com.zakgof.tools.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

public class Xml {
  private final String name;
  private String value;
  private final List<Xml> children = new ArrayList<>();
  private Xml parent;
  private final Map<String, String> params = new LinkedHashMap<>();

  public Xml() {
    this.name = null;
  }

  public Xml append(Xml tag) {
    children.add(tag);
    return this;
  }

  public Xml append(Stream<Xml> tags) {
    children.addAll(tags.collect(Collectors.toList()));
    return this;
  }

  public Xml p(String key, String value) {
    params.put(key, value);
    return this;
  }

  public Xml done() {
    return parent;
  }

  public Xml v(String value) {
    this.value = value;
    return this;
  }

  public Xml(Xml parent, String name) {
    this.parent = parent;
    this.name = name;
  }

  public Xml tag(String name) {
    Xml t = new Xml(this, name);
    this.append(t);
    return t;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (name != null) {
      sb.append("<").append(name);
      Stream.of(params.entrySet()).forEach(e -> {
        sb.append(" ").append(e.getKey()).append("=\"").append(e.getValue()).append("\"");
      });
      sb.append(">");
    }
    if (value != null)
      sb.append(value);
    children.forEach(ch -> sb.append(ch.toString()));
    if (name != null) {
      sb.append("</").append(name).append(">");
    }
    return sb.toString();
  }

}