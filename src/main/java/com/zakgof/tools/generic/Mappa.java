package com.zakgof.tools.generic;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class Mappa {

  public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    Builder<K, V> builder = ImmutableMap.<K, V> builder();
    if (v1 != null)
      builder.put(k1, v1);
    if (v2 != null)
      builder.put(k2, v2);
    if (v3 != null)
      builder.put(k3, v3);
    if (v4 != null)
      builder.put(k4, v4);
    if (v5 != null)
      builder.put(k5, v5);
    return builder.build();
  }

}
