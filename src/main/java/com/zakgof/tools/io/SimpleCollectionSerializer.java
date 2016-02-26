package com.zakgof.tools.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class SimpleCollectionSerializer<T, S extends ISimpleSerializer<T>> implements ISimpleSerializer<Collection<T>> {

  private final S elementSerializer;

  public SimpleCollectionSerializer(S elementSerializer) {
    this.elementSerializer = elementSerializer;
  }

  @Override
  public void write(SimpleOutputStream out, Collection<T> val) throws IOException {
    out.write(val.size());
    for (final T t : val)
      elementSerializer.write(out, t);
  }

  @Override
  public Collection<T> read(SimpleInputStream in) throws IOException {
    final int length = in.readInt();
    final Collection<T> arr = new ArrayList<T>(length);
    for (int i = 0; i < length; i++)
      arr.add(elementSerializer.read(in));
    return arr;
  }

}
