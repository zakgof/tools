package com.zakgof.tools.generic;

import java.util.Iterator;
import java.util.function.Function;

import com.annimon.stream.Stream;
import com.google.common.base.Predicate;

public class Streams {

  public static <T> Stream<T> nonNullStream(T first, Function<T, T> generator, Predicate<T> validator) {

    final Iterator<T> it = new Iterator<T>() {

      private T next = first;

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public T next() {
        T ret = next;
        next = generator.apply(next);
        if (!validator.apply(next))
          next = null;
        return ret;
      }
    };
    return Stream.of(it);
  }

}
