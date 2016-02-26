package com.zakgof.tools;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

interface ICollectionFactory {
	<K, V>	Map<K, V> createMap();
	<V> Set<V> createList();
}

public class MultiMapImpl<K, V> implements MultiMap<K, V> {

	private final Map<K, Set<V>> map;
	private final ICollectionFactory factory;

	protected MultiMapImpl(ICollectionFactory factory) {
		this.factory = factory;
		map = factory.createMap();
	}

	@Override
	public void put(K key, V value) {
		Set<V> list = map.get(key);
		if (list == null) {
			list = factory.createList();
			map.put(key, list);
		}
		list.add(value);
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public Set<V> get(K key) {
		final Set<V> set = map.get(key);
		return set == null ? new HashSet<V>() : set;
	}

	@Override
	public Set<V> getE(K key) {
		final Set<V> list = map.get(key);
		return (list==null) ? factory.<V>createList() : list;
	}

	@Override
	public Set<V> allValues() {
		final Set<V> res = factory.createList();
		for(final Set<V> list : map.values())
			for(final V value : list)
				res.add(value);
		return res;
	}

	@Override
	public void remove(K key) {
	  map.remove(key);
	}


  @Override
  public void remove(K key, V value) {
    map.get(key).remove(value);
  }
}
