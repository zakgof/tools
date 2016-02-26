package com.zakgof.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiHashMap<Key, Value> extends MultiMapImpl<Key, Value> {

	public MultiHashMap() {
		super(new ICollectionFactory() {
			@Override
			public <K, V> Map<K, V> createMap() {
				return new HashMap<K, V>();
			}

			@Override
			public <V> Set<V> createList() {
				return new HashSet<V>();
			}
		});
	}

}
