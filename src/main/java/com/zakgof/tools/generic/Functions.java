package com.zakgof.tools.generic;

public class Functions {

  public static <T> IFunction<T, T> identity() {
    return new IFunction<T, T>() {
      @Override
      public T get(T arg) {
        return arg;
      }
    };
  }
  
  public static <T> IProvider<T> firstNext(IProvider<T> first, IFunction<T, T> next) {
    return new IProvider<T>() {
      
      private T current;

      @Override
      public T get() {
        if (current == null)
          return (current = first.get());
        return (current = next.get(current));
      }
    };
  }

}
