package com.digitalbarista.cat.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MultiValueMap<X,Y,Z> {

	private Map<X,Y> map1 = new HashMap<X,Y>();
	private Map<X,Z> map2 = new HashMap<X,Z>();
	
	public void clear() {
		map1.clear();
		map2.clear();
	}
	public boolean containsKey(Object key) {
		return map1.containsKey(key);
	}
	public Y getValue1(Object key) {
		return map1.get(key);
	}
	public Z getValue2(Object key) {
		return map2.get(key);
	}
	public Set<X> keySet() {
		return map1.keySet();
	}
	public void put(X key, Y value1, Z value2) {
		map1.put(key, value1);
		map2.put(key, value2);
	}
	public void remove(Object key) {
		map1.remove(key);
		map2.remove(key);
	}
	public int size() {
		return map1.size();
	}
	
}
