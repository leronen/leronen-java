package util.collections;

import java.util.*;

public class TwoLevelHashMap<K1, K2, V> {

	Map<K1,Map<K2, V>> mOuterMap = new LinkedHashMap<K1,Map<K2, V>>();
	
	public V get(K1 k1, K2 k2) {
		if (mOuterMap.containsKey(k1)) {
			return mOuterMap.get(k1).get(k2);
		}
		else {
			return null;
		}
	}
	
	public void put(K1 k1, K2 k2, V v) {
		Map innerMap = mOuterMap.get(k1); 
		
		if (innerMap == null) {
			innerMap = new LinkedHashMap<K2, V>();
			mOuterMap.put(k1, innerMap);
		}
		innerMap.put(k2, v);
	}
	
	public Set<K1> keySet() {
		return mOuterMap.keySet();
	}
	
	public Map<K2, V> get(K1 k1) {
		return mOuterMap.get(k1);
	}
	
}
