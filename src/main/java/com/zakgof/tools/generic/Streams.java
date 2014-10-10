package com.zakgof.tools.generic;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false);
  }

}
