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

}
