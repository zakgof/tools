package com.zakgof.tools;

import java.util.Set;


public interface MultiMap<K, V> {

	void put(K key, V value);

	Set<K> keySet();

	Set<V> get(K key);

	Set<V> getE(K key);

	Set<V> allValues();

  void remove(K key);

  void remove(K key, V value);
}
