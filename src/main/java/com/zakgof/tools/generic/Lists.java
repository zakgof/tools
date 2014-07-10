package com.zakgof.tools.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Lists {
  
  public static <F, T> List<T> transform(Collection<? extends F> src, IFunction<F, ? extends T> func) {
    List<T> dest = new ArrayList<T>(src.size());
    for (F f : src) {
      dest.add(func.get(f));
    }
    return dest;
  }

}
