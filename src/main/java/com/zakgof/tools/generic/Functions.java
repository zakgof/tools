package com.zakgof.tools.generic;

import java.util.Comparator;

public class Functions {

  public static <T> IFunction<T, T> identity() {
    return new IFunction<T, T>() {
      @Override
      public T get(T arg) {
        return arg;
      }
    };
  }
  
  public static <T extends Comparable<T>> Comparator<T> comparator() {
    return new Comparator<T>() {
      @Override
      public int compare(T o1, T o2) {
        return o1.compareTo(o2);
      }
    };
  }
  
  public static <T extends Comparable<T>> Comparator<T> reverseComparator() {
    return new Comparator<T>() {
      @Override
      public int compare(T o1, T o2) {
        return -o1.compareTo(o2);
      }
    };
  }
  
  public static <T, K extends Comparable<K>> Comparator<T> comparator(IFunction<T, K> getter) {
    return new Comparator<T>() {
      @Override
      public int compare(T o1, T o2) {
        return getter.get(o1).compareTo(getter.get(o2));
      }
    };
  }
  
  public static <T, K extends Comparable<K>> Comparator<T> reverseComparator(IFunction<T, K> getter) {
    return new Comparator<T>() {
      @Override
      public int compare(T o1, T o2) {
        return -getter.get(o1).compareTo(getter.get(o2));
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
