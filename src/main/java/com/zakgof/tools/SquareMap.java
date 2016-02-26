package com.zakgof.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class CommPair<K> {

	private final K k1;
	private final K k2;

	CommPair(K k1, K k2) {
		this.k1 = k1;
		this.k2 = k2;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CommPair<?>) {
			@SuppressWarnings("unchecked")
      final
			CommPair<K> that = (CommPair<K>) obj;
			return this.k1.equals(that.k1) && this.k2.equals(that.k2);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return k1.hashCode() + 31 * k2.hashCode();
	}

}

public class SquareMap<K, V> {

	private final Map<CommPair<K>, V> map = new HashMap<CommPair<K>, V>();

	public V get(K key1, K key2) {
		return map.get(new CommPair<K>(key1, key2));
	}

	public void put(K key1, K key2, V value) {
		map.put(new CommPair<K>(key1, key2), value);
	}

	public Collection<V> values() {
	  return map.values();
	}

}
