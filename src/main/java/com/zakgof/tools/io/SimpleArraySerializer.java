package com.zakgof.tools.io;

import java.io.IOException;

public class SimpleArraySerializer<T, S extends ISimpleSerializer<T>> implements ISimpleSerializer<T[]> {

  private final S elementSerializer;

  public SimpleArraySerializer(S elementSerializer) {
    this.elementSerializer = elementSerializer;
  }

  @Override
  public void write(SimpleOutputStream out, T[] val) throws IOException {
    out.write(val.length);
    for (final T t : val)
      elementSerializer.write(out, t);
  }

  @Override
  public T[] read(SimpleInputStream in) throws IOException {
    final int length = in.readInt();
    @SuppressWarnings("unchecked")
    final T[] arr = (T[]) new Object[length];
    for (int i = 0; i < length; i++)
      arr[i] = elementSerializer.read(in);
    return arr;
  }

}
