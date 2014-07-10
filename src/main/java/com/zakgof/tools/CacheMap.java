package com.zakgof.tools;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CacheMap<K, V> {
  
  private final Map<K, V> cache = new HashMap<K, V>();
  private final Queue<K> usage = new LinkedList<K>();
  private final int maxSize;

  public CacheMap(int maxSize) {
    this.maxSize = maxSize;
  }

  public void put(K key, V value) {
    cache.put(key, value);
    usage.remove(key);
    usage.add(key);
    while (cache.size() > maxSize) {
      K expired = usage.poll();
      cache.remove(expired);
    }
  }

  public V get(K key) {
    usage.remove(key);
    usage.add(key);
    return cache.get(key);
  }
  
  

}
